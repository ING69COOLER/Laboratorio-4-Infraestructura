import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Cliente TCP para conversión entre sistemas numéricos.
 * 
 * Protocolo de comunicación (separado por ";"):
 *   Operación 1: DEC_BIN;<número_decimal>;<longitud_bits>
 *   Operación 2: BIN_DEC;<número_binario>
 *   Operación 3: DEC_HEX;<número_decimal>;<ancho_digitos>
 *   Operación 4: HEX_DEC;<número_hexadecimal>
 *   Operación 5: BIN_HEX;<número_binario>;<ancho_digitos>
 *   Operación 6: HEX_BIN;<número_hexadecimal>
 */
public class EchoTCPClient {

    private static final Scanner SCANNER = new Scanner(System.in);

    public static final String SERVER = "10.112.94.80";
    public static final int PORT = 3400;

    private PrintWriter toNetwork;
    private BufferedReader fromNetwork;
    private Socket clientSideSocket;

    public EchoTCPClient() {
        System.out.println("=== Cliente TCP - Conversión entre Sistemas Numéricos ===");
    }

    public void init() throws Exception {
        clientSideSocket = new Socket(SERVER, PORT);
        System.out.println("[Conectado al servidor " + SERVER + ":" + PORT + "]");

        createStreams(clientSideSocket);
        protocol(clientSideSocket);

        clientSideSocket.close();
        System.out.println("[Conexión cerrada]");
    }

    public void protocol(Socket socket) throws Exception {
        boolean continuar = true;

        while (continuar) {
            mostrarMenu();
            System.out.print("Seleccione una opción: ");
            String opcion = SCANNER.nextLine().trim();

            String mensaje = null;

            switch (opcion) {
                case "1":
                    mensaje = construirDecBin();
                    break;
                case "2":
                    mensaje = construirBinDec();
                    break;
                case "3":
                    mensaje = construirDecHex();
                    break;
                case "4":
                    mensaje = construirHexDec();
                    break;
                case "5":
                    mensaje = construirBinHex();
                    break;
                case "6":
                    mensaje = construirHexBin();
                    break;
                case "0":
                    continuar = false;
                    System.out.println("Saliendo...");
                    break;
                default:
                    System.out.println("[ERROR] Opción inválida. Intente de nuevo.");
                    break;
            }

            if (mensaje != null) {
                System.out.println("[Cliente] Enviando al servidor: " + mensaje);
                toNetwork.println(mensaje);

                String respuesta = fromNetwork.readLine();
                System.out.println("[Cliente] Respuesta del servidor: " + respuesta);
                System.out.println();
            }
        }
    }

    // ─── Constructores de mensajes ──────────────────────────────────────────────

    /** Operación 1: Decimal → Binario con longitud en bits */
    private String construirDecBin() {
        System.out.print("  Número decimal: ");
        String numero = SCANNER.nextLine().trim();
        System.out.print("  Longitud en bits: ");
        String longitud = SCANNER.nextLine().trim();
        return "DEC_BIN;" + numero + ";" + longitud;
    }

    /** Operación 2: Binario → Decimal */
    private String construirBinDec() {
        System.out.print("  Número binario: ");
        String numero = SCANNER.nextLine().trim();
        return "BIN_DEC;" + numero;
    }

    /** Operación 3: Decimal → Hexadecimal con ancho en dígitos */
    private String construirDecHex() {
        System.out.print("  Número decimal: ");
        String numero = SCANNER.nextLine().trim();
        System.out.print("  Ancho en dígitos hexadecimales: ");
        String ancho = SCANNER.nextLine().trim();
        return "DEC_HEX;" + numero + ";" + ancho;
    }

    /** Operación 4: Hexadecimal → Decimal */
    private String construirHexDec() {
        System.out.print("  Número hexadecimal: ");
        String numero = SCANNER.nextLine().trim();
        return "HEX_DEC;" + numero;
    }

    /** Operación 5: Binario → Hexadecimal con ancho en dígitos */
    private String construirBinHex() {
        System.out.print("  Número binario: ");
        String numero = SCANNER.nextLine().trim();
        System.out.print("  Ancho en dígitos hexadecimales: ");
        String ancho = SCANNER.nextLine().trim();
        return "BIN_HEX;" + numero + ";" + ancho;
    }

    /** Operación 6: Hexadecimal → Binario */
    private String construirHexBin() {
        System.out.print("  Número hexadecimal: ");
        String numero = SCANNER.nextLine().trim();
        return "HEX_BIN;" + numero;
    }

    // ─── Utilidades ─────────────────────────────────────────────────────────────

    private void mostrarMenu() {
        System.out.println("──────────────────────────────────────");
        System.out.println("  1. Decimal-->Binario  (con longitud en bits)");
        System.out.println("  2. Binario-->Decimal");
        System.out.println("  3. Decimal-->Hexadecimal  (con ancho en dígitos)");
        System.out.println("  4. Hexadecimal-->Decimal");
        System.out.println("  5. Binario-->Hexadecimal  (con ancho en dígitos)");
        System.out.println("  6. Hexadecimal-->Binario");
        System.out.println("  0. Salir");
        System.out.println("──────────────────────────────────────");
    }

    private void createStreams(Socket socket) throws Exception {
        toNetwork = new PrintWriter(socket.getOutputStream(), true);
        fromNetwork = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public static void main(String[] args) throws Exception {
        EchoTCPClient ec = new EchoTCPClient();
        ec.init();
    }
}
