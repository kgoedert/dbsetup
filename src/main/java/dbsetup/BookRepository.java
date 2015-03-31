package dbsetup;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;


@Repository
public interface BookRepository extends EntityRepository<Book, Long>, QueryDslSupport{

}
