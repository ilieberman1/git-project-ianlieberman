import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class gitTester {
    private static MessageDigest md;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        clearDir(new File("git"));
        clearDir(new File("root"));
        md = MessageDigest.getInstance("SHA-1");

        initRepoTester();
        // DirectoryTester();
        commitTester();

        clearDir(new File("git"));
        clearDir(new File("root"));
    }

    public static void commitTester() throws IOException, NoSuchAlgorithmException {
        Git git = new Git();
        File OutsideFolder = new File("./root");
        File InsideFolder = new File("./root/bak");
        File InsideFile = new File("./root/new.txt");
        File InsideInsideFile = new File("./root/bak/test.txt");
        File InsideFile2 = new File("./root/test.txt");

        if (!OutsideFolder.exists()) {
            OutsideFolder.mkdir();
        }
        if (!InsideFolder.exists()) {
            InsideFolder.mkdir();

        }
        if (!InsideFile2.exists()) {
            InsideFile2.createNewFile();
            BufferedWriter writer = Files.newBufferedWriter(InsideFile2.toPath());
            writer.write("version 2");
            writer.close();

        }
        if (!InsideFile.exists()) {
            InsideFile.createNewFile();
            BufferedWriter writer = Files.newBufferedWriter(InsideFile.toPath());
            writer.write("new file");
            writer.close();

        }
        if (!InsideInsideFile.exists()) {
            InsideInsideFile.createNewFile();
            BufferedWriter writer = Files.newBufferedWriter(InsideInsideFile.toPath());
            writer.write("version 1");
            writer.close();
        }

        String c1 = git.commit("Hugh", "harro");

        File ExtraFile = new File("./root/omg.txt");
        ExtraFile.createNewFile();
        git.stage(ExtraFile.getPath());
        String c2 = git.commit("Hugh", "OHMYGOODNESS");
        if (c1 != c2)
            System.out.println("commit working");
    }

    public static void DirectoryTester() throws IOException, NoSuchAlgorithmException {

        File OutsideFolder = new File("./root");
        File InsideFolder = new File("./root/bak");
        File InsideFile = new File("./root/new.txt");
        File InsideInsideFile = new File("./root/bak/test.txt");
        File InsideFile2 = new File("./root/test.txt"); // doesn't work currently

        if (!OutsideFolder.exists()) {
            OutsideFolder.mkdir();
        }
        if (!InsideFolder.exists()) {
            InsideFolder.mkdir();

        }
        if (!InsideFile2.exists()) {
            InsideFile2.createNewFile();
            BufferedWriter writer = Files.newBufferedWriter(InsideFile2.toPath());
            writer.write("version 2");
            writer.close();

        }
        if (!InsideFile.exists()) {
            InsideFile.createNewFile();
            BufferedWriter writer = Files.newBufferedWriter(InsideFile.toPath());
            writer.write("new file");
            writer.close();

        }
        if (!InsideInsideFile.exists()) {
            InsideInsideFile.createNewFile();
            BufferedWriter writer = Files.newBufferedWriter(InsideInsideFile.toPath());
            writer.write("version 1");
            writer.close();
        }
        Git.newDirectoryBlob(OutsideFolder.toPath());

    }

    public static boolean initRepoTester() throws IOException { // Runs initRepo and checks if the repo was initted
        File gitDir = new File("./git");

        Git.initRepo();

        if (Files.exists(Path.of("./git/objects")) && Files.exists(Path.of("./git/index"))) {
            for (File f : gitDir.listFiles()) {
                System.out.println(f.getName());
                // f.delete();
            }
            System.out.println("initRepo worked");
            return true;
        }
        System.out.println("initRepo failed");
        return false;
    }

    public static void clearDir(File dir) throws IOException { // Recursively clears a directory, 30% sure this doesn't
                                                               // work
        try {
            for (File f : dir.listFiles()) {
                if (f.isDirectory()) {
                    clearDir(f);
                } else {
                    f.delete();
                }
            }
            dir.delete();
        } catch (FileNotFoundException e) {
            System.out.println("clearDir failed");
        }

    }

    public static boolean newBlobTester() throws IOException, NoSuchAlgorithmException { // Makes 100 files filled
                                                                                         // randomly and stores array of
                                                                                         // expected hashed names,
                                                                                         // creates blobs for all the
                                                                                         // files, and returns false if
                                                                                         // any of the files don't match
                                                                                         // up when compared
        File blobTests = new File("./git/blobTests");
        System.out.println(blobTests.mkdir());

        String[] blobNames = new String[10];

        for (int i = 0; i < blobNames.length; i++) {
            File f = new File("./git/blobTests/" + i);
            f.createNewFile();
            blobNames[i] = fillRandAndSHA(new File("./git/blobTests/" + i));

            Git.newBlob(new File("./git/blobTests/" + i), false, new File("./git/blobTests/" + i).getPath());
        }
        File directory = new File("./git/blobTests/directory");

        for (int i = 0; i < blobNames.length; i++) {
            if (!Files.exists(Path.of("./git/objects/" + blobNames[i]))) {
                return false;
            }
        }

        // clearDir(new File("./git/blobTests"));
        // Files.deleteIfExists(Path.of("./git/blobTests"));

        return true;
    }

    public static String fillRandAndSHA(File f) throws IOException { // Takes file, inputs 500 random digits 1-9, then
                                                                     // returns the SHA1 name it should have
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            sb.append((int) (Math.random() * 9));
        }

        Files.writeString(Path.of(f.getPath()), sb);

        String hex = "";
        for (byte i : md.digest(sb.toString().getBytes())) {
            hex += String.format("%02X", i);
        }

        return hex;
    }
    /*
     * MAKE TESTER MAKE TESTERMAKE TESTERMAKE TESTERMAKE TESTERMAKE TESTERMAKE
     * TESTERMAKE TESTERMAKE TESTERMAKE TESTERMAKE TESTERMAKE TESTERMAKE TESTERMAKE
     * TESTERMAKE TESTERMAKE TESTERMAKE TESTERMAKE TESTERMAKE TESTERMAKE TESTERMAKE
     * TESTERMAKE TESTERMAKE TESTERMAKE TESTERMAKE TESTERMAKE TESTERMAKE TESTERMAKE
     * TESTERMAKE TESTERMAKE TESTERMAKE TESTERMAKE TESTERMAKE TESTERMAKE TESTERMAKE
     * TESTERMAKE TESTERMAKE TESTER
     */
}
