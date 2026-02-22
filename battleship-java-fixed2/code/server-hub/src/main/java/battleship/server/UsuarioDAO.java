package battleship.server;

import battleship.model.Usuario;

import javax.persistence.*;
import java.util.List;

/**
 * Capa de persistencia con JPA para la entidad Usuario.
 */
public class UsuarioDAO {

    private static EntityManagerFactory emf;

    static {
        try {
            emf = Persistence.createEntityManagerFactory("battleshipPU");
        } catch (Exception e) {
            System.err.println("[DAO] Error al inicializar JPA: " + e.getMessage());
            throw e;
        }
    }

    private EntityManager getEM() {
        return emf.createEntityManager();
    }

    /** Busca un usuario por username y password */
    public Usuario findByCredentials(String username, String password) {
        EntityManager em = getEM();
        try {
            return em.createQuery(
                    "SELECT u FROM Usuario u WHERE u.username = :u AND u.password = :p",
                    Usuario.class)
                    .setParameter("u", username)
                    .setParameter("p", password)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    /** Busca un usuario por username */
    public Usuario findByUsername(String username) {
        EntityManager em = getEM();
        try {
            return em.createQuery(
                    "SELECT u FROM Usuario u WHERE u.username = :u",
                    Usuario.class)
                    .setParameter("u", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    /** Crea un nuevo usuario */
    public Usuario create(Usuario usuario) {
        EntityManager em = getEM();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(usuario);
            tx.commit();
            return usuario;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /** Actualiza estadísticas de un usuario */
    public void updateStats(int userId, int ganadas, int perdidas, int puntos) {
        EntityManager em = getEM();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Usuario u = em.find(Usuario.class, userId);
            if (u != null) {
                u.setPartidasGanadas(u.getPartidasGanadas() + ganadas);
                u.setPartidasPerdidas(u.getPartidasPerdidas() + perdidas);
                u.setPuntosTotales(u.getPuntosTotales() + puntos);
                em.merge(u);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /** Lista todos los usuarios ordenados por puntos */
    public List<Usuario> findAll() {
        EntityManager em = getEM();
        try {
            return em.createQuery(
                    "SELECT u FROM Usuario u ORDER BY u.puntosTotales DESC",
                    Usuario.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public static void shutdown() {
        if (emf != null && emf.isOpen()) emf.close();
    }
}
