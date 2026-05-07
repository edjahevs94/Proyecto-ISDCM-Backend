package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Video;
import util.ConexionBD;

public class VideoDAOBackend {
    
        public boolean actualizarVideo(Video v) {
        String sql = "UPDATE Videos SET titulo=?, autor=?, fecha_creacion=?, duracion=?, descripcion=?, formato=?, rutafichero=? WHERE id=?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, v.getTitulo());
            ps.setString(2, v.getAutor());
            ps.setString(3, v.getFechaCreacion());
            ps.setString(4, v.getDuracion());
            ps.setString(5, v.getDescripcion());
            ps.setString(6, v.getFormato());
            ps.setString(7, v.getRutaFichero());
            ps.setInt(8, v.getId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Video> buscarPorTitulo(String titulo) {
        List<Video> lista = new ArrayList<>();
        String sql = "SELECT * FROM Videos WHERE UPPER(Titulo) LIKE UPPER(?)";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + titulo + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Video v = new Video();
                v.setId(rs.getInt("ID"));
                v.setTitulo(rs.getString("Titulo"));
                v.setAutor(rs.getString("Autor"));
                v.setFechaCreacion(rs.getDate("Fecha_creacion").toString());
                v.setDuracion(rs.getTime("Duracion").toString());
                v.setReproducciones(rs.getInt("Reproducciones"));
                v.setDescripcion(rs.getString("Descripcion"));
                v.setFormato(rs.getString("Formato"));
                v.setRutaFichero(rs.getString("RutaFichero"));
                v.setUsuarioId(rs.getInt("UsuarioId"));
                lista.add(v);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Video> buscarPorAutor(String autor) {
        List<Video> lista = new ArrayList<>();
        String sql = "SELECT * FROM Videos WHERE UPPER(Autor) LIKE UPPER(?)";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + autor + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Video v = new Video();
                v.setId(rs.getInt("ID"));
                v.setTitulo(rs.getString("Titulo"));
                v.setAutor(rs.getString("Autor"));
                v.setFechaCreacion(rs.getDate("Fecha_creacion").toString());
                v.setDuracion(rs.getTime("Duracion").toString());
                v.setReproducciones(rs.getInt("Reproducciones"));
                v.setDescripcion(rs.getString("Descripcion"));
                v.setFormato(rs.getString("Formato"));
                v.setRutaFichero(rs.getString("RutaFichero"));
                v.setUsuarioId(rs.getInt("UsuarioId"));
                lista.add(v);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Video> buscarPorFecha(String fecha) {
        List<Video> lista = new ArrayList<>();
        String sql;

        try {
            String[] partes = fecha.split("-");

            if (partes.length == 1) {
                sql = "SELECT * FROM Videos WHERE YEAR(Fecha_creacion) = ?";
                try (Connection con = ConexionBD.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, Integer.parseInt(fecha));
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        lista.add(mapearVideo(rs));
                    }
                }
            } else if (partes.length == 2) {
                int anio = Integer.parseInt(partes[0]);
                int mes = Integer.parseInt(partes[1]);
                sql = "SELECT * FROM Videos WHERE YEAR(Fecha_creacion) = ? AND MONTH(Fecha_creacion) = ?";
                try (Connection con = ConexionBD.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, anio);
                    ps.setInt(2, mes);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        lista.add(mapearVideo(rs));
                    }
                }
            } else {
                sql = "SELECT * FROM Videos WHERE Fecha_creacion = ?";
                try (Connection con = ConexionBD.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setDate(1, java.sql.Date.valueOf(fecha));
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        lista.add(mapearVideo(rs));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }

    private Video mapearVideo(ResultSet rs) throws SQLException {
        Video v = new Video();
        v.setId(rs.getInt("ID"));
        v.setTitulo(rs.getString("Titulo"));
        v.setAutor(rs.getString("Autor"));
        v.setFechaCreacion(rs.getDate("Fecha_creacion").toString());
        v.setDuracion(rs.getTime("Duracion").toString());
        v.setReproducciones(rs.getInt("Reproducciones"));
        v.setDescripcion(rs.getString("Descripcion"));
        v.setFormato(rs.getString("Formato"));
        v.setRutaFichero(rs.getString("RutaFichero"));
        v.setUsuarioId(rs.getInt("UsuarioId"));
        return v;
    }

    public boolean incrementarReproducciones(int videoId) {
        String sql = "UPDATE Videos SET Reproducciones = Reproducciones + 1 WHERE ID = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, videoId);
            int filasActualizadas = ps.executeUpdate();
            return filasActualizadas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Video getVideoPorId(int videoId) {
        String sql = "SELECT * FROM Videos WHERE ID = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, videoId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapearVideo(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
