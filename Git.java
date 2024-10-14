import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Git implements GitInterface {
    public static boolean doCompress = false;
    // private static MessageDigest md;
    // public Git (boolean doCompress) {
    // this.doCompress = doCompress;
    // }

    public Git() {
    }

    public static void initRepo() throws IOException {

        if (Files.exists(Path.of("./git/objects")) && Files.exists(Path.of("./git/index"))
                && Files.exists(Path.of("./git/HEAD"))) {

            System.out.println("Git Repository already exists");
        } else {
            if (!Files.exists(Path.of("./git"))) {
                File newDir = new File("./git");
                newDir.mkdir();
            }

            if (!Files.exists(Path.of("./git/objects"))) {
                File newDir = new File("./git/objects");
                newDir.mkdir();
            }

            if (!Files.exists(Path.of("./git/index"))) {
                Files.createFile(Path.of("./git/index"));
            }

            if (!Files.exists(Path.of("./git/HEAD"))) {
                Files.createFile(Path.of("./git/HEAD"));
            }
        }
    } // Checks if a git repo exists, if not, makes essential files, redundantly
      // checking if they already exist on the way

    public String commit(String author, String message) {
        try {
            StringBuilder sb = new StringBuilder();

            File root = new File("root");
            sb.append("tree: " + hashFile(root) + "\n");

            File head = new File("git/HEAD");
            sb.append("parent: " + Files.readString(head.toPath()) + "\n");

            sb.append("author: " + author + "\n");

            sb.append("date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy")) + "\n");

            sb.append("message: " + message + "\n");

            String hash = hashFile(sb.toString().getBytes());
            File commitFile = new File("git/objects/" + hash);
            FileWriter commitWriter = new FileWriter(commitFile);
            commitWriter.write(sb.toString());
            commitWriter.close();
            FileWriter headWriter = new FileWriter(head, false);
            headWriter.write(hash);
            headWriter.close();

            File index = new File("git/index");
            index.delete();
            index.createNewFile();

            return hash;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public void stage(String filePath) {
        try {
            File stageFile = new File(filePath);
            if (stageFile.isDirectory())
                newDirectoryBlob(stageFile.toPath());
            else
                newBlob(stageFile, false, stageFile.getPath());
            File root = new File("root");
            newDirectoryBlob(root.toPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkout(String commitHash) {
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] treeToBytes(File input) throws IOException, NoSuchAlgorithmException {
        StringBuilder sb = new StringBuilder();
        for (File f : input.listFiles()) {
            String fName;
            if (f.getAbsolutePath().equals(f.getCanonicalPath())) {
                fName = f.getName();
            } else {
                fName = f.getAbsolutePath();
            }
            if (f.isDirectory()) {
                String fHash = hashFile(treeToBytes(f));
                sb.append("tree " + fHash + " " + fName + "\n");
            } else {
                String fHash = hashFile(Files.readAllBytes(Paths.get(f.getPath())));
                sb.append("blob " + fHash + " " + fName + "\n");
            }
        }
        return sb.toString().getBytes();
    }

    public static String hashFile(byte[] fContents) throws IOException, NoSuchAlgorithmException { // Checks if file
                                                                                                   // exists, if
        // so, it reads the contents
        // into a byte array, converts
        // that into SHA then returns
        // hex
        MessageDigest md = MessageDigest.getInstance("SHA-1");

        String hex = "";
        for (byte i : md.digest(fContents)) {
            hex += String.format("%02X", i);
        }

        return hex;
    }

    public static String hashFile(File f) throws IOException, NoSuchAlgorithmException { // Checks if file exists, if
                                                                                         // so, it reads the contents
                                                                                         // into a byte array, converts
                                                                                         // that into SHA then returns
                                                                                         // hex

        if (!Files.exists(f.toPath())) {
            throw new FileNotFoundException();
        }
        byte[] fContents;
        if (f.isDirectory()) {
            fContents = treeToBytes(f);
        } else {
            fContents = Files.readAllBytes(Path.of(f.getPath()));
        }

        return hashFile(fContents);
    } // Reads out files contents into byte array then hashes it with MessageDigest
      // with SHA-1

    public static void newBlob(File f, boolean tree, String path) throws IOException, NoSuchAlgorithmException {
        // Hashes OG file data to make name, save new file to objects folder, zip and
        // copy data into new file, and add blob name and OG File name into index file

        File blob = new File("./git/objects/" + hashFile(f));
        if (blob.exists())
            return;
        if (!tree) {
            blob.createNewFile();
        }
        File index = new File("./git/index");
        BufferedWriter writer = Files.newBufferedWriter(index.toPath(), StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
        if (tree) {
            writer.append("tree ");
        } else {
            writer.append("blob ");
        }
        if (doCompress) {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(blob));

            zos.putNextEntry(new ZipEntry(f.getPath()));

            zos.write(Files.readAllBytes(f.toPath()));
            zos.closeEntry();
            zos.close();

            Files.writeString(Path.of("./git/index"), blob.getName() + " " + f.getName() + '\n');
        }
        // If they dont want to compress, reads bytes from OG File and converts to
        // charsequence/string for Files.writeString
        else {
            if (!tree) {
                Files.write(Path.of(blob.getPath()),
                        new String(Files.readAllBytes(Path.of(f.getPath())), StandardCharsets.UTF_8).getBytes(),
                        StandardOpenOption.APPEND);
            }

            writer.append(blob.getName() + " " + path + '\n');

            writer.close();
        }

    }

    public static void newDirectoryBlob(Path p) throws NoSuchAlgorithmException, IOException {
        if (!p.toFile().exists()) {
            throw new FileNotFoundException("Directory doesn't currently exist");
        }
        File f = p.toFile();
        if (!f.isDirectory()) {
            newBlob(f, false, f.getPath());
            // return hashFile(f);
        } else {
            File[] files = f.listFiles();
            for (int i = 0; i < files.length; i++) {
                newDirectoryBlob(files[i].toPath());
            }

            File tree = createTree(f);
            newBlob(tree, true, f.getPath());
        }
    }

    public static File createTree(File f) throws IOException, NoSuchAlgorithmException {
        if (!f.isDirectory()) {
            System.out.println("not a directory, can't make a tree");
            return null;
        } else {
            File fileName = new File("./git/objects/" + hashFile(f));
            if (!fileName.exists()) {
                Files.createFile(fileName.toPath());
            }
            FileOutputStream writer = new FileOutputStream(fileName.getPath());
            writer.write(treeToBytes(f));
            writer.close();
            return fileName;
        }
    }
    /*
     * References
     * http://www.sha1-online.com/sha1-java/
     * https://docs.oracle.com/javase/8/docs/api/java/security/MessageDigest.html
     * https://stackoverflow.com/questions/858980/file-to-byte-in-java
     * https://stackoverflow.com/questions/3849692/whole-text-file-to-a-string-in-
     * java
     * https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/file/
     * Files.html#readString(java.nio.file.Path)
     * https://docs.oracle.com/javase/8/docs/api/java/io/File.html
     * https://stackoverflow.com/questions/1536054/how-to-convert-byte-array-to-
     * string-and-vice-versa
     * https://www.geeksforgeeks.org/java-program-to-convert-byte-array-to-hex-
     * string/
     * https://www.codejava.net/java-se/file-io/how-to-compress-files-in-zip-format-
     * in-java
     */
}