package ewm.participationRequest.repository;

import ewm.participationRequest.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RequestStatusRepository extends JpaRepository<RequestStatus, Byte> {
    Optional<RequestStatus> findByName(String name);
}
