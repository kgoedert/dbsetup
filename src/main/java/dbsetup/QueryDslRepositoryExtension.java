package dbsetup;

import com.mysema.query.jpa.impl.JPAQuery;
import org.apache.deltaspike.data.spi.DelegateQueryHandler;
import org.apache.deltaspike.data.spi.QueryInvocationContext;

import javax.inject.Inject;

/**
 * Created by kelly on 30/03/15.
 */
public class QueryDslRepositoryExtension<E> implements QueryDslSupport, DelegateQueryHandler {
    @Inject
    private QueryInvocationContext context;

    @Override
    public JPAQuery jpaQuery()
    {
        return new JPAQuery(context.getEntityManager());
    }
}
