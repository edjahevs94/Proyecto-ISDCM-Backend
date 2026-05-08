package rest;

import dao.UsuarioDAOBackend;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import modelo.Usuario;
import util.JwtUtil;

@Path("/usuarios")
public class UsuarioResource {

    private final UsuarioDAOBackend dao = new UsuarioDAOBackend();

    @POST
    @Path("/registro")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registrar(Usuario usuario) {

        if (usuario.getName() == null || usuario.getName().isBlank()
                || usuario.getLastname() == null || usuario.getLastname().isBlank()
                || usuario.getEmail() == null || usuario.getEmail().isBlank()
                || usuario.getUsername() == null || usuario.getUsername().isBlank()
                || usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"estado\":\"error\",\"mensaje\":\"Todos los campos son obligatorios\"}")
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        }

        String validacion = dao.validateFields(usuario.getEmail(), usuario.getUsername());

        if ("email".equals(validacion)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"estado\":\"error\",\"mensaje\":\"El email ya está registrado\"}")
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        }
        if ("username".equals(validacion)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"estado\":\"error\",\"mensaje\":\"El nombre de usuario ya existe\"}")
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        }
        if ("error_db".equals(validacion)) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"estado\":\"error\",\"mensaje\":\"Error al conectar con la base de datos\"}")
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        }

        boolean insertado = dao.insertarUsuario(usuario);

        if (insertado) {
            return Response.status(Response.Status.CREATED)
                    .entity("{\"estado\":\"exito\",\"mensaje\":\"Usuario registrado correctamente\"}")
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"estado\":\"error\",\"mensaje\":\"Error al registrar el usuario\"}")
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        }
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(Usuario credenciales) {

        if (credenciales.getUsername() == null || credenciales.getUsername().isBlank()
                || credenciales.getPassword() == null || credenciales.getPassword().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"estado\":\"error\",\"mensaje\":\"Usuario y contraseña son obligatorios\"}")
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        }

        Usuario usuario = dao.loginUsuario(credenciales.getUsername(), credenciales.getPassword());

        if (usuario == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"estado\":\"error\",\"mensaje\":\"Usuario o contraseña incorrectos\"}")
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        }

        String token = JwtUtil.generarToken(usuario.getId(), usuario.getUsername());

        String json = "{\"estado\":\"exito\",\"mensaje\":\"Login correcto\""
                + ",\"id\":" + usuario.getId()
                + ",\"name\":\"" + usuario.getName() + "\""
                + ",\"lastname\":\"" + usuario.getLastname() + "\""
                + ",\"email\":\"" + usuario.getEmail() + "\""
                + ",\"username\":\"" + usuario.getUsername() + "\""
                + ",\"token\":\"" + token + "\"}";

        return Response.ok(json, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    @OPTIONS
    @Path("{path: .*}")
    public Response options() {
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type")
                .build();
    }
}
