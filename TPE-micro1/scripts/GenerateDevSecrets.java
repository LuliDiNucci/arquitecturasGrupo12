import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.util.*;

/** Java 21: genera material local; nunca imprime claves ni contraseñas. */
public class GenerateDevSecrets {
    private static final SecureRandom RANDOM = new SecureRandom();
    public static void main(String[] args) throws Exception {
        Path root = Path.of(args.length == 0 ? "." : args[0]).toAbsolutePath().normalize();
        Path secrets = root.resolve(".secrets");
        Path env = root.resolve(".env");
        if (Files.exists(secrets.resolve("jwt-private.pem")) || Files.exists(secrets.resolve("jwt-public.pem")) || Files.exists(env))
            throw new IllegalStateException("Ya existe configuración local. No se sobrescribe; use otra carpeta para una configuración nueva.");
        Files.createDirectories(secrets);
        var generator = KeyPairGenerator.getInstance("RSA"); generator.initialize(3072);
        KeyPair pair = generator.generateKeyPair();
        writePem(secrets.resolve("jwt-private.pem"), "PRIVATE KEY", pair.getPrivate().getEncoded());
        writePem(secrets.resolve("jwt-public.pem"), "PUBLIC KEY", pair.getPublic().getEncoded());
        List<String> lines = new ArrayList<>();
        lines.add("# Sólo desarrollo local. No versionar ni compartir este archivo.");
        for (String service : List.of("USUARIOS", "EVENTOS", "RESERVAS", "COMPRAS", "ENTRADAS")) {
            lines.add(service + "_DB_PASSWORD=" + secret());
        }
        lines.add("MYSQL_ROOT_PASSWORD=" + secret());
        for (String service : List.of("EVENTOS", "RESERVAS", "COMPRAS", "ENTRADAS")) {
            lines.add("SERVICE_" + service + "_SECRET=" + secret());
        }
        lines.add("BOOTSTRAP_ADMIN_ENABLED=true");
        lines.add("BOOTSTRAP_ADMIN_EMAIL=admin@ticketya.local");
        lines.add("BOOTSTRAP_ADMIN_PASSWORD=" + secret());
        Files.write(env, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        System.out.println("Se generaron .secrets y .env en " + root + ". Mantenga ambos fuera de Git.");
    }
    private static String secret() {
        byte[] bytes = new byte[32]; RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    private static void writePem(Path path, String name, byte[] encoded) throws Exception {
        String body = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(encoded);
        Files.writeString(path, "-----BEGIN " + name + "-----\n" + body + "\n-----END " + name + "-----\n",
            StandardCharsets.US_ASCII, StandardOpenOption.CREATE_NEW);
    }
}
