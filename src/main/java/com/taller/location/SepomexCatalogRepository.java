package com.taller.location;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;

@Repository
public class SepomexCatalogRepository {
    private static final URI EXPORT_URL = URI.create("https://www.correosdemexico.gob.mx/sslservicios/consultacp/CodigoPostal_Exportar.aspx");
    private final HttpClient client = HttpClient.newBuilder().cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)).connectTimeout(Duration.ofSeconds(20)).build();

    public List<AddressEntry> download() {
        try {
            String page = client.send(HttpRequest.newBuilder(EXPORT_URL).GET().timeout(Duration.ofSeconds(30)).build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.ISO_8859_1)).body();
            String form = "__VIEWSTATE=" + encode(hidden(page, "__VIEWSTATE")) + "&__VIEWSTATEGENERATOR=" + encode(hidden(page, "__VIEWSTATEGENERATOR")) + "&__EVENTVALIDATION=" + encode(hidden(page, "__EVENTVALIDATION")) + "&cboEdo=00&rblTipo=txt&btnDescarga.x=1&btnDescarga.y=1";
            HttpRequest request = HttpRequest.newBuilder(EXPORT_URL).timeout(Duration.ofSeconds(120)).header("Content-Type", "application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(form)).build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) throw new IOException("Respuesta no válida de SEPOMEX");
            return parse(response.body());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No fue posible consultar el catálogo oficial de SEPOMEX en este momento");
        }
    }

    private List<AddressEntry> parse(byte[] compressedCatalog) throws IOException {
        List<AddressEntry> entries = new ArrayList<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(compressedCatalog))) {
            if (zip.getNextEntry() == null) throw new IOException("Archivo SEPOMEX vacío");
            String content = new String(zip.readAllBytes(), StandardCharsets.ISO_8859_1);
            for (String line : content.split("\\R")) {
                String[] values = line.split("\\|", -1);
                if (values.length < 12 || !values[0].matches("\\d{5}")) continue;
                entries.add(new AddressEntry(values[7], values[4].trim(), values[11], values[3].trim(), values[1].trim(), values[0]));
            }
        }
        if (entries.isEmpty()) throw new IOException("Archivo SEPOMEX sin registros");
        return entries;
    }

    private String hidden(String html, String name) throws IOException {
        Matcher matcher = Pattern.compile("<input[^>]*name=\"" + Pattern.quote(name) + "\"[^>]*value=\"([^\"]*)\"", Pattern.CASE_INSENSITIVE).matcher(html);
        if (!matcher.find()) throw new IOException("Formulario SEPOMEX no disponible");
        return matcher.group(1);
    }
    private String encode(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }

    public record AddressEntry(String stateCode, String state, String municipalityCode, String municipality, String neighborhood, String postalCode) { }
}
