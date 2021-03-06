/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.edu.um.mateo.inscripciones.dao.impl;

import java.util.HashMap;
import java.util.Map;
import mx.edu.um.mateo.general.dao.BaseDao;
import mx.edu.um.mateo.general.model.Usuario;
import mx.edu.um.mateo.general.utils.Constantes;
import mx.edu.um.mateo.inscripciones.dao.AlumnoPaqueteDao;
import mx.edu.um.mateo.inscripciones.model.AlumnoPaquete;
import mx.edu.um.mateo.inscripciones.model.TiposBecas;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author semdariobarbaamaya
 */
@Repository
@Transactional
public class AlumnoPaqueteDaoHibernate extends BaseDao implements AlumnoPaqueteDao {

    @Override
    public Map<String, Object> lista(Map<String, Object> params) {
        log.debug("Buscando lista de Tipos de Becas con params {}", params);
        if (params == null) {
            params = new HashMap<>();
        }

        if (!params.containsKey("max")) {
            params.put("max", 10);
        } else {
            params.put("max", Math.min((Integer) params.get("max"), 100));
        }

        if (params.containsKey("pagina")) {
            Long pagina = (Long) params.get("pagina");
            Long offset = (pagina - 1) * (Integer) params.get("max");
            params.put("offset", offset.intValue());
        }

        if (!params.containsKey("offset")) {
            params.put("offset", 0);
        }
        Criteria criteria = currentSession().createCriteria(AlumnoPaquete.class);
        Criteria countCriteria = currentSession().createCriteria(AlumnoPaquete.class);

        log.debug("Empresa trae {}", params.get("empresa"));
        if (params.containsKey("empresa")) {
            criteria.createCriteria("paquete.empresa").add(
                    Restrictions.idEq(params.get("empresa")));
            countCriteria.createCriteria("paquete").createCriteria("empresa").add(
                    Restrictions.idEq(params.get("empresa")));
        }

        if (params.containsKey("filtro")) {
            String filtro = (String) params.get("filtro");
            Disjunction propiedades = Restrictions.disjunction();
            propiedades.add(Restrictions.ilike("matricula", filtro,//descripcion
                    MatchMode.ANYWHERE));
//            propiedades.add(Restrictions.ilike("nombre", filtro,
//                    MatchMode.ANYWHERE));
            criteria.add(propiedades);
            countCriteria.add(propiedades);
        }

        if (params.containsKey("order")) {
            String campo = (String) params.get("order");
            if (params.get("sort").equals("desc")) {
                criteria.addOrder(Order.desc(campo));
            } else {
                criteria.addOrder(Order.asc(campo));
            }
        } else {
            criteria.addOrder(Order.asc("matricula"));//descripcion
        }

        if (!params.containsKey("reporte")) {
            criteria.setFirstResult((Integer) params.get("offset"));
            criteria.setMaxResults((Integer) params.get("max"));
        }
        params.put(Constantes.CONTAINSKEY_ALUMNOPAQUETES, criteria.list());

        countCriteria.setProjection(Projections.rowCount());
        params.put("cantidad", (Long) countCriteria.list().get(0));

        return params;
    }

    @Override
    public AlumnoPaquete obtiene(final Long id) {
        AlumnoPaquete alumnoPaquete = (AlumnoPaquete) currentSession().get(AlumnoPaquete.class, id);
        if (alumnoPaquete == null) {
            //log.warn("uh oh, tipoBeca with id '" + id + "' not found...");
            throw new ObjectRetrievalFailureException(AlumnoPaquete.class, id);
        }

        return alumnoPaquete;
    }

    public void graba(final AlumnoPaquete alumnoPaquete, Usuario usuario) {
        Session session = currentSession();
        if (usuario != null) {
            alumnoPaquete.getPaquete().setEmpresa(usuario.getEmpresa());
        }
        currentSession().saveOrUpdate(alumnoPaquete);
        currentSession().merge(alumnoPaquete);
        currentSession().flush();
    }

    @Override
     public AlumnoPaquete actualiza(AlumnoPaquete alumnoPaquete) {
        Session session = currentSession();
        
        session.update(alumnoPaquete);
        session.flush();
        return alumnoPaquete;
    }
    
    @Override
    public String elimina(final Long id) {
        AlumnoPaquete alumnoPaquete = this.obtiene(id);
        String matricula = alumnoPaquete.getMatricula();
        currentSession().delete(alumnoPaquete);
        currentSession().flush();
        return matricula;
    }
}
