import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Servidor TCP para conversión entre sistemas numéricos.
 *
 * Protocolo esperado del cliente (separado por ";"):
 * DEC_BIN;<decimal>;<longitud_bits>
 * BIN_DEC;<binario>
 * DEC_HEX;<decimal>;<ancho_digitos>
 * HEX_DEC;<hexadecimal>
 * BIN_HEX;<binario>;<ancho_digitos>
 * HEX_BIN;<hexadecimal>
 */
public class EchoTCPServer {

    public static final int PORT = 3400;

    private ServerSocket listener;

    public EchoTCPServer() {
        System.out.println("=== Servidor TCP - Conversión entre Sistemas Numéricos ===");
        System.out.println("Escuchando en puerto: " + PORT);
    }

    public void init() throws Exception {
        listener = new ServerSocket(PORT);

        while (true) {
            Socket clientSocket = listener.accept();
            System.out.println("\n[Nuevo cliente conectado: "
                    + clientSocket.getInetAddress().getHostAddress() + "]");

            // Manejar cada cliente en un hilo separado para permitir conexiones simultáneas
            new Thread(() -> {
                try {
                    handleClient(clientSocket);
                } catch (Exception e) {
                    System.out.println("[Error cliente] " + e.getMessage());
                } finally {
                    try {
                        clientSocket.close();
                        System.out.println("[Cliente desconectado. Esperando nuevos clientes...]");
                    } catch (Exception ex) {
                        System.out.println("[Error cerrando socket] " + ex.getMessage());
                    }
                }
            }).start();
        }
    }

    private void handleClient(Socket socket) throws Exception {
        PrintWriter toNetwork = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader fromNetwork = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String message;

        while ((message = fromNetwork.readLine()) != null) {
            System.out.println("[Server] Recibido: " + message);

            String respuesta = procesarMensaje(message);
            System.out.println("[Server] Enviando: " + respuesta);

            toNetwork.println(respuesta);
        }
    }

    // ─── Procesamiento del protocolo

    private String procesarMensaje(String mensaje) {
        try {
            String[] partes = mensaje.split(";");
            String operacion = partes[0].toUpperCase();

            switch (operacion) {
                case "1": {
                    // 1;<decimal>;<longitud_bits>
                    long numero = Long.parseLong(partes[1]);
                    int longitud = Integer.parseInt(partes[2]);
                    return decimalABinario(numero, longitud);
                }
                case "2": {
                    // 2;<binario>
                    return String.valueOf(binarioADecimal(partes[1]));
                }
                case "3": {
                    // 3;<decimal>;<ancho_digitos>
                    long numero = Long.parseLong(partes[1]);
                    int ancho = Integer.parseInt(partes[2]);
                    return decimalAHexadecimal(numero, ancho);
                }
                case "4": {
                    // 4;<hexadecimal>
                    return String.valueOf(hexadecimalADecimal(partes[1]));
                }
                case "5": {
                    // 5;<binario>;<ancho_digitos>
                    long decimal = binarioADecimal(partes[1]);
                    int ancho = Integer.parseInt(partes[2]);
                    return decimalAHexadecimal(decimal, ancho);
                }
                case "6": {
                    // 6;<hexadecimal>
                    long decimal = hexadecimalADecimal(partes[1]);
                    // Sin longitud fija: usamos múltiplo de 4 (nibbles)
                    int longitud = Math.max(4, partes[1].length() * 4);
                    return decimalABinario(decimal, longitud);
                }
                default:
                    return "ERROR;Operacion desconocida: " + operacion;
            }

        } catch (ArrayIndexOutOfBoundsException e) {
            return "ERROR;Faltan parametros en el mensaje";
        } catch (NumberFormatException e) {
            return "ERROR;Formato de numero invalido";
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    // ─── Funciones de conversión 
    /**
     * Convierte un número decimal a binario con la longitud especificada en bits.
     * Ejemplo: decimalABinario(10, 16) → "0000000000001010"
     */
    private String decimalABinario(long numero, int longitud) {
        String binario = Long.toBinaryString(numero);
        // Si el binario es más largo que la longitud pedida, se retorna tal cual
        if (binario.length() >= longitud) {
            return binario;
        }
        // Rellenar con ceros a la izquierda
        return String.format("%" + longitud + "s", binario).replace(' ', '0');
    }

    /**
     * Convierte un número binario (String) a decimal.
     * Ejemplo: binarioADecimal("110110") → 54
     */
    private long binarioADecimal(String binario) {
        return Long.parseLong(binario, 2);
    }

    /**
     * Convierte un número decimal a hexadecimal con el ancho especificado.
     * Ejemplo: decimalAHexadecimal(271, 4) → "010F"
     */
    private String decimalAHexadecimal(long numero, int ancho) {
        String hex = Long.toHexString(numero).toUpperCase();
        if (hex.length() >= ancho) {
            return hex;
        }
        return String.format("%" + ancho + "s", hex).replace(' ', '0');
    }

    /**
     * Convierte un número hexadecimal (String) a decimal.
     * Ejemplo: hexadecimalADecimal("3E1") → 993
     */
    private long hexadecimalADecimal(String hex) {
        return Long.parseLong(hex, 16);
    }

    // ─── Utilidades

    public static void main(String[] args) throws Exception {
        EchoTCPServer es = new EchoTCPServer();
        es.init();
    }
}
