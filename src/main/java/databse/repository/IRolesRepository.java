package databse.repository;

import databse.model.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IRolesRepository extends JpaRepository<RoleEntity, Integer> {
    Optional<RoleEntity> findByNome(String role);
}
