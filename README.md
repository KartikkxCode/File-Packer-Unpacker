# 🔐 File Packer & Unpacker with Encryption (Java + Swing)

**Author:** Kartik Anilrao Dahale  

This project is a **Java-based file utility tool** that allows you to pack multiple files into a single encrypted archive and later unpack them securely.  
It provides both **file metadata preservation** and **AES encryption** for security.  
A **Swing GUI** makes the application user-friendly and easy to operate.

---

## ✨ Features

- 📦 **File Packing**
  - Combines multiple files from a directory into a single archive.
  - Stores file metadata (name, size, last modified timestamp).

- 📂 **File Unpacking**
  - Extracts files from an encrypted archive.
  - Restores metadata and saves files in a dedicated `Unpacked_Files` folder.

- 🔒 **Security**
  - AES-256 encryption with PBKDF2 key derivation.
  - User provides a password at the time of packing/unpacking.
  - Magic header (`FPACK`) ensures file integrity and password validation.

- 🖥️ **Graphical User Interface (GUI)**
  - Built with Java Swing.
  - Separate tabs for **Packer** and **Unpacker**.
  - Integrated log panel to track actions.

- ⚙️ **Cross-platform**
  - Runs on any system with Java Runtime Environment (JRE 11+).

---

## 🛠️ Requirements

- **Java JDK 11+** (JDK 17 recommended)
- No external libraries required (uses built-in Java APIs)

---

## 🚀 How to Compile & Run

1. Save the file as:

```bash
FilePackerUnpacker.java
