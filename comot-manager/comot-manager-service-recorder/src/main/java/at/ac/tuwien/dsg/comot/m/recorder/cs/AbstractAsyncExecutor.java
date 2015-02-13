package at.ac.tuwien.dsg.comot.m.recorder.cs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.m.core.dal.ServiceRepoProxy;
import at.ac.tuwien.dsg.comot.m.core.model.ServiceEntity;
import at.ac.tuwien.dsg.comot.m.recorder.revisions.RevisionApi;

public abstract class AbstractAsyncExecutor {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private static final long TIMEOUT = 10000;
	protected ManagedService mService;

	@Autowired
	protected ServiceRepoProxy serviceRepo;
	@Autowired
	protected RevisionApi revisionApi;

	@Async
	public void start(ManagedService mService) {

		try {

			this.mService = mService;

			while (true) {

				log.info("async executor, timeout {} ms", TIMEOUT);

				ServiceEntity entity = serviceRepo.findOne(mService.getServiceId());

				// stop if recording stopped in DB
				if (!entity.getRecording()) {
					return;
				}

				oneIteration();

				try {
					Thread.sleep(TIMEOUT);
				} catch (InterruptedException e) {
					log.info("Failed to sleep ", e);
				}
			}

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	abstract protected void oneIteration() throws CoreServiceException, ComotException, IllegalArgumentException,
			IllegalAccessException;

}
