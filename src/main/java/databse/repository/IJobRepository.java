package databse.repository;

import databse.model.JobEntity;
import databse.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IJobRepository extends JpaRepository<JobEntity, Integer> {
}
