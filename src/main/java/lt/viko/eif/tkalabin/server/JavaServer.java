package lt.viko.eif.tkalabin.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class JavaServer {
    private ServerSocket serverSocket;
    private Socket clientSocket;

    private static DataOutputStream dataOutputStream;
    private static DataInputStream dataInputStream;

    /**
     * Starts server instance locally
     * @param port by default - 6666
     */

    public void StartServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started. Port: " + port);
            clientSocket = serverSocket.accept();

            dataInputStream = new DataInputStream(clientSocket.getInputStream());
            dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
            System.out.println("Client connected." + clientSocket);

            String filename = dataInputStream.readUTF();

            System.out.println("Filename: " + filename);

            receiveFile(filename);


        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        finally {
            stop();
        }

    }

    /**
     * Stops server instance
     */

    public void stop() {
        try {
            dataOutputStream.close();
            dataInputStream.close();
            clientSocket.close();
            serverSocket.close();
        }
        catch (IOException e) {
            System.out.println("Exception occured: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        JavaServer javaServer = new JavaServer();
        javaServer.StartServer(6666);
    }

    /**
     * Receives file from client and check, if file is valid by XSD schema
     * @param fileName - name of file, that is being received
     * @throws IOException
     */
    private static void receiveFile(String fileName) throws IOException {
        try {

            int bytes = 0;
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);

            long size = dataInputStream.readLong();     // read file size
            byte[] buffer = new byte[4096];
            while (size > 0 && (bytes = dataInputStream.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
                fileOutputStream.write(buffer,0,bytes);
                size -= bytes;      // read upto file size
            }
            fileOutputStream.close();

            String rez = "File received. ";

            if(XMLValidator.validateXMLSchema("publishers.xsd", fileName)) {
                dataOutputStream.writeUTF(rez + "File is valid!");
            }
            else {
                dataOutputStream.writeUTF(rez + "File in invalid.");
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }
}