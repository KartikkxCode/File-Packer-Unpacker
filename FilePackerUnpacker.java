import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

/**
 * File Packer & Unpacker with Encryption
 * Author: Kartik Anilrao Dahale
 */
public class FilePackerUnpacker extends JFrame {

    // GUI components
    private JTextField txtDirPath, txtPackFile, txtUnpackFile;
    private JButton btnDirBrowse, btnPack, btnFileBrowse, btnUnpack;
    private JTextArea txtLog;

    // Magic header to validate packed files
    private static final String MAGIC = "FPACK";

    public FilePackerUnpacker() {
        setTitle("File Packer & Unpacker with Encryption");
        setSize(800, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        // Packer tab
        JPanel packPanel = new JPanel(new GridLayout(3, 3, 10, 10));
        txtDirPath = new JTextField();
        txtPackFile = new JTextField();
        btnDirBrowse = new JButton("Browse");
        btnPack = new JButton("Start Packing");

        packPanel.add(new JLabel("Select Directory to Pack:"));
        packPanel.add(txtDirPath);
        packPanel.add(btnDirBrowse);

        packPanel.add(new JLabel("Packed File Name:"));
        packPanel.add(txtPackFile);
        packPanel.add(new JLabel());

        packPanel.add(new JLabel());
        packPanel.add(btnPack);
        packPanel.add(new JLabel());

        tabbedPane.addTab("Packer", packPanel);

        // Unpacker tab
        JPanel unpackPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        txtUnpackFile = new JTextField();
        btnFileBrowse = new JButton("Browse");
        btnUnpack = new JButton("Start Unpacking");

        unpackPanel.add(new JLabel("Select Packed File:"));
        unpackPanel.add(txtUnpackFile);
        unpackPanel.add(btnFileBrowse);

        unpackPanel.add(new JLabel());
        unpackPanel.add(btnUnpack);
        unpackPanel.add(new JLabel());

        tabbedPane.addTab("Unpacker", unpackPanel);

        // Log Area
        txtLog = new JTextArea();
        txtLog.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(txtLog);

        add(tabbedPane, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Event Listeners
        btnDirBrowse.addActionListener(e -> chooseDirectory());
        btnFileBrowse.addActionListener(e -> chooseFile());
        btnPack.addActionListener(e -> startPacking());
        btnUnpack.addActionListener(e -> startUnpacking());

        setVisible(true);
    }

    /* ------------------ GUI Actions ------------------ */
    private void chooseDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtDirPath.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Packed Files", "pack"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtUnpackFile.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void startPacking() {
        String dirPath = txtDirPath.getText().trim();
        String packName = txtPackFile.getText().trim();
        if (dirPath.isEmpty() || packName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Provide directory path and packed file name.");
            return;
        }
        String password = JOptionPane.showInputDialog(this, "Enter password to encrypt:");
        if (password == null || password.isEmpty()) return;
        new Thread(() -> packDirectory(dirPath, packName, password.toCharArray())).start();
    }

    private void startUnpacking() {
        String filePath = txtUnpackFile.getText().trim();
        if (filePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a packed file.");
            return;
        }
        String password = JOptionPane.showInputDialog(this, "Enter password to decrypt:");
        if (password == null || password.isEmpty()) return;
        new Thread(() -> unpackFile(filePath, password.toCharArray())).start();
    }

    /* ------------------ Packing ------------------ */
    private void packDirectory(String dirName, String packName, char[] password) {
        try {
            clearLog();
            log("=== Packing Started ===");
            File dir = new File(dirName);
            if (!dir.exists() || !dir.isDirectory()) {
                log("Directory not found.");
                return;
            }

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(bout);

            File[] files = dir.listFiles();
            if (files == null) return;

            dout.writeInt(files.length); // number of files
            for (File file : files) {
                if (file.isFile()) {
                    byte[] content = Files.readAllBytes(file.toPath());
                    long modified = file.lastModified();

                    dout.writeUTF(file.getName());
                    dout.writeLong(content.length);
                    dout.writeLong(modified);
                    dout.write(content);

                    log("Packed: " + file.getName());
                }
            }
            dout.flush();

            byte[] archive = bout.toByteArray();
            byte[] encrypted = encrypt(archive, password);

            try (FileOutputStream fos = new FileOutputStream(packName)) {
                fos.write(MAGIC.getBytes(StandardCharsets.UTF_8)); // header
                fos.write(encrypted);
            }

            log("Packing complete. Saved to " + packName);
        } catch (Exception e) {
            log("Error: " + e.getMessage());
        }
    }

    /* ------------------ Unpacking ------------------ */
    private void unpackFile(String packName, char[] password) {
        try {
            clearLog();
            log("=== Unpacking Started ===");

            File packedFile = new File(packName);
            if (!packedFile.exists()) {
                log("Packed file not found.");
                return;
            }

            byte[] allData = Files.readAllBytes(packedFile.toPath());
            String magic = new String(allData, 0, MAGIC.length(), StandardCharsets.UTF_8);
            if (!MAGIC.equals(magic)) {
                log("Invalid packed file format.");
                return;
            }

            byte[] encrypted = new byte[allData.length - MAGIC.length()];
            System.arraycopy(allData, MAGIC.length(), encrypted, 0, encrypted.length);

            byte[] plain = decrypt(encrypted, password);
            DataInputStream din = new DataInputStream(new ByteArrayInputStream(plain));

            int count = din.readInt();
            File outputDir = new File("Unpacked_Files");
            outputDir.mkdir();

            for (int i = 0; i < count; i++) {
                String name = din.readUTF();
                long size = din.readLong();
                long modified = din.readLong();
                byte[] content = din.readNBytes((int) size);

                File outFile = new File(outputDir, name);
                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    fos.write(content);
                }
                outFile.setLastModified(modified);
                log("Unpacked: " + outFile.getName());
            }
            log("Unpacking complete. Files saved in: " + outputDir.getAbsolutePath());
        } catch (Exception e) {
            log("Error: " + e.getMessage());
        }
    }

    /* ------------------ Encryption ------------------ */
    private byte[] encrypt(byte[] data, char[] password) throws Exception {
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[16]; sr.nextBytes(salt);
        byte[] iv = new byte[16]; sr.nextBytes(iv);

        SecretKey key = deriveKey(password, salt);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] cipherText = cipher.doFinal(data);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        bout.write(salt);
        bout.write(iv);
        bout.write(cipherText);
        return bout.toByteArray();
    }

    private byte[] decrypt(byte[] payload, char[] password) throws Exception {
        ByteArrayInputStream bin = new ByteArrayInputStream(payload);
        byte[] salt = bin.readNBytes(16);
        byte[] iv = bin.readNBytes(16);
        byte[] cipherText = bin.readAllBytes();

        SecretKey key = deriveKey(password, salt);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        return cipher.doFinal(cipherText);
    }

    private SecretKey deriveKey(char[] password, byte[] salt) throws Exception {
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password, salt, 65536, 256);
        SecretKey tmp = skf.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    /* ------------------ Logging ------------------ */
    private void clearLog() {
        SwingUtilities.invokeLater(() -> txtLog.setText(""));
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> txtLog.append(msg + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FilePackerUnpacker::new);
    }
}
