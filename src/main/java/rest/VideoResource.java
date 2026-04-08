/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package rest;

import dao.VideoDAOBackend;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import modelo.Video;

import java.util.List;

@Path("/videos")
public class VideoResource {

    private final VideoDAOBackend dao = new VideoDAOBackend();

    @GET
    @Path("/buscar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscarVideos(
            @QueryParam("titulo") String titulo,
            @QueryParam("autor") String autor,
            @QueryParam("fecha") String fecha) {

        List<Video> resultados;

        if (titulo != null && !titulo.isEmpty() && fecha == null && autor == null) {
            resultados = dao.buscarPorTitulo(titulo);
        } else if (autor != null && !autor.isEmpty() && fecha == null && titulo == null) {
            resultados = dao.buscarPorAutor(autor);
        } else if (fecha != null && !fecha.isEmpty() && titulo == null && autor == null) {
            resultados = dao.buscarPorFecha(fecha);
        } else {
            resultados = buscarAvanzada(titulo, autor, fecha);
        }

        String json = convertirAJSONArray(resultados);

        return Response.ok(json, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    private List<Video> buscarAvanzada(String titulo, String autor, String fecha) {
        List<Video> todos = dao.buscarPorTitulo("");

        todos.removeIf(v -> {
            boolean cumple = true;

            if (titulo != null && !titulo.isEmpty()) {
                cumple = cumple && v.getTitulo().toLowerCase().contains(titulo.toLowerCase());
            }

            if (autor != null && !autor.isEmpty()) {
                cumple = cumple && v.getAutor().toLowerCase().contains(autor.toLowerCase());
            }

            if (fecha != null && !fecha.isEmpty()) {
                cumple = cumple && coincidirFecha(v.getFechaCreacion(), fecha);
            }

            return !cumple;
        });

        return todos;
    }

    private boolean coincidirFecha(String fechaVideo, String criterioFecha) {
        if (criterioFecha == null || criterioFecha.isEmpty()) {
            return true;
        }

        String[] partes = criterioFecha.split("-");

        try {
            if (partes.length == 1) {
                return fechaVideo.startsWith(partes[0]);
            } else if (partes.length == 2) {
                return fechaVideo.startsWith(partes[0] + "-" + partes[1]);
            } else {
                return fechaVideo.equals(criterioFecha);
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Incrementar contador de reproducciones
     *
     * POST /api/videos/{id}/reproduccion
     *
     * Parámetro:
     * - id: ID del vídeo (path parameter)
     *
     * Respuesta:
     * - JSON con estado y nueva cantidad de reproducciones
     */
    @POST
    @Path("/{id}/reproduccion")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response incrementarReproduccion(@PathParam("id") int id) {

        if (id <= 0) {
            String error = "{\"estado\":\"error\",\"mensaje\":\"ID de vídeo inválido\"}";
            return Response.status(Response.Status.BAD_REQUEST).entity(error)
                    .header("Access-Control-Allow-Origin", "*").build();
        }

        Video video = dao.getVideoPorId(id);
        if (video == null) {
            String error = "{\"estado\":\"error\",\"mensaje\":\"Vídeo no encontrado\"}";
            return Response.status(Response.Status.NOT_FOUND).entity(error)
                    .header("Access-Control-Allow-Origin", "*").build();
        }

        boolean exito = dao.incrementarReproducciones(id);

        if (exito) {
            String respuesta = "{\"estado\":\"exito\",\"mensaje\":\"Reproducción registrada\",\"nuevo_contador\":"
                    + (video.getReproducciones() + 1) + "}";
            return Response.ok(respuesta, MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*").build();
        } else {
            String error = "{\"estado\":\"error\",\"mensaje\":\"Error al actualizar reproducciones\"}";
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error)
                    .header("Access-Control-Allow-Origin", "*").build();
        }
    }

    /**
     * Obtener un vídeo por ID
     *
     * GET /api/videos/{id}
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerVideo(@PathParam("id") int id) {
        Video video = dao.getVideoPorId(id);

        if (video == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Vídeo no encontrado\"}")
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        }

        String json = "[" + convertirAJSONObject(video) + "]";

        return Response.ok(json, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    /**
     * Convertir un Video a objeto JSON (como String)
     */
    private String convertirAJSONObject(Video v) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(v.getId());
        sb.append(",\"titulo\":").append(escapeJson(v.getTitulo()));
        sb.append(",\"autor\":").append(escapeJson(v.getAutor()));
        sb.append(",\"fechaCreacion\":").append(escapeJson(v.getFechaCreacion()));
        sb.append(",\"duracion\":").append(escapeJson(v.getDuracion()));
        sb.append(",\"reproducciones\":").append(v.getReproducciones());
        sb.append(",\"descripcion\":").append(escapeJson(v.getDescripcion()));
        sb.append(",\"formato\":").append(escapeJson(v.getFormato()));
        sb.append(",\"rutaFichero\":").append(escapeJson(v.getRutaFichero()));
        sb.append(",\"usuarioId\":").append(v.getUsuarioId());
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convertir lista de Videos a array JSON (como String)
     */
    private String convertirAJSONArray(List<Video> videos) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (int i = 0; i < videos.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(convertirAJSONObject(videos.get(i)));
        }

        sb.append("]");
        return sb.toString();
    }

    /**
     * Escapar caracteres especiales para JSON
     */
    private String escapeJson(String s) {
        if (s == null) {
            return "\"\"";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(c);
            }
        }
        sb.append("\"");
        return sb.toString();
    }
}
