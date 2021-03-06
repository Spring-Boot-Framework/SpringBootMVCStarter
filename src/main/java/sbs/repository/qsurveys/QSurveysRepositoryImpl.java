package sbs.repository.qsurveys;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import sbs.model.qsurveys.QSurvey;
import sbs.repository.GenericRepositoryAdapter;

@Repository("qSurveysRepositoryImpl")
@Transactional
public class QSurveysRepositoryImpl extends GenericRepositoryAdapter<QSurvey, Integer>
		implements QSurveysRepository {

	@Override
	public List<QSurvey> findAllSortByDateDesc() {
		String hql = "from QSurvey s ORDER BY creationDate DESC";
		@SuppressWarnings("unchecked")
		List<QSurvey> result = (List<QSurvey>) currentSession().createQuery(hql).list();
		return result;
	}

}
