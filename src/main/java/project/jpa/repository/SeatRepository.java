package project.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.jpa.domain.Seat;
import project.jpa.enums.SpaceType;

public interface SeatRepository extends JpaRepository<Seat, Long>, SeatRepositoryCustom {

}
