package databse.repository;

import databse.model.AppointmentsEntity;
import databse.model.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IAppointmentsRepository extends JpaRepository<AppointmentsEntity, Integer> {
    List<AppointmentsEntity> findByData(List data);
}
