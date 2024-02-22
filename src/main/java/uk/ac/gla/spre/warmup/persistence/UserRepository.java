package uk.ac.gla.spre.warmup.persistence;

import org.springframework.data.repository.CrudRepository;
import java.util.List;
public interface UserRepository extends CrudRepository<User, Integer> {
        List<User> findByName(String name);
}
