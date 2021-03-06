package sbs.service.qcheck;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import sbs.model.qcheck.QCheck;
import sbs.repository.GenericRepository;
import sbs.repository.qcheck.QCheckRepository;
import sbs.service.GenericServiceAdapter;

@Service
public class QCheckServiceImpl extends GenericServiceAdapter<QCheck, Integer> implements QCheckService{
	
	private QCheckRepository qCheckRepository;
	
    @Autowired
	public QCheckServiceImpl(@Qualifier("qCheckRepositoryImpl") GenericRepository<QCheck, Integer> genericRepository) {
			super(genericRepository);
			this.qCheckRepository = (QCheckRepository) genericRepository;
	}

	@Override
	public List<QCheck> findAllPending() {
		return qCheckRepository.findAllPending();
	}

	@Override
	public List<QCheck> findAllClosed() {
		return qCheckRepository.findAllClosed();
	}

	@Override
	public List<QCheck> findAllWithStateOrder(int order) {
		return qCheckRepository.findAllWithStateOrder(order);
	}

}
