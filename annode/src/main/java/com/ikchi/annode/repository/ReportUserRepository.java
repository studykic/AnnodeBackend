package com.ikchi.annode.repository;

import com.ikchi.annode.domain.entity.ReportUserAndPospace;
import com.ikchi.annode.domain.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class ReportUserRepository {

    @PersistenceContext
    private EntityManager em;

    public void save(ReportUserAndPospace reportUserAndPospace) {
        em.persist(reportUserAndPospace);
    }

    public List<ReportUserAndPospace> findReportUserAndPospaceListByUser(User user) {
        return em.createQuery("SELECT rp FROM ReportUserAndPospace rp WHERE rp.reporter = :user")
            .setParameter("user", user)
            .getResultList();
    }


    public void delete(ReportUserAndPospace reportUserAndPospace) {
        em.remove(reportUserAndPospace);
    }

}
