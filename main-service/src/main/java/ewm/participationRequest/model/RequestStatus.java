package ewm.participationRequest.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table (name = "request_statuses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequestStatus {
    @Id
    private Byte id;

    @Column(unique = true)
    private String name;
}
