# File Packer & Unpacker with Encryption

**Author:** Kartik Anilrao Dahale

This Java project packs multiple files into a single archive and encrypts it with a password. It also supports decrypting and unpacking the archive, restoring original file metadata (name, size, lastModified). A Swing GUI is included.

## Build / Run (Maven)

1. Build with Maven:
```bash
mvn clean package
```

2. Run GUI:
```bash
java -cp target/file-packer-unpacker-1.0.0.jar com.kartik.filepacker.gui.FilePackerGUI
```

3. CLI examples:
```bash
java -cp target/file-packer-unpacker-1.0.0.jar com.kartik.filepacker.App pack myArchive.mpack file1.txt image.png
java -cp target/file-packer-unpacker-1.0.0.jar com.kartik.filepacker.App unpack myArchive.mpack outputDir
```
