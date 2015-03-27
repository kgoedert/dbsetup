package dbsetup;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.hibernate.StatelessSession;
import org.hibernate.jpa.HibernateEntityManagerFactory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.archive.importer.MavenImporter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.mysema.query.jpa.impl.JPAQuery;
import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.DbSetupTracker;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.generator.ValueGenerators;
import com.ninja_squad.dbsetup.operation.Insert;
import com.ninja_squad.dbsetup.operation.Operation;

@RunWith(Arquillian.class)
public class BookTest {

	@Resource(lookup = "java:/dbsetupDS")
	private DataSource dataSource;

	@PersistenceContext
	private EntityManager em;

	private static DbSetupTracker dbSetupTracker = new DbSetupTracker();
	
	@Inject
	private BookRepository bookRepo;

	@Deployment
	public static WebArchive createDeployment() {

		PomEquippedResolveStage pom = Maven.resolver().loadPomFromFile("pom.xml");
		WebArchive war = ShrinkWrap.create(MavenImporter.class).loadPomFromFile("pom.xml").importBuildOutput().as(WebArchive.class);
		war.addAsLibraries(pom.resolve("com.ninja-squad:DbSetup:1.3.1").withTransitivity().asFile());
		war.addClass(CommonOperations.class);
		war.addAsWebInfResource("jbossas-ds.xml", "jbossas-ds.xml").addAsResource("META-INF/persistence.xml", "/META-INF/persistence.xml");

		return war;
	}

	/**
	 * Insere os dados que serão utilizados por todos os testes
	 */
	@Before
	public void setup() {
		Operation inicialOps = Operations.sequenceOf(CommonOperations.DELETE_ALL,
				Operations.insertInto("BOOK").columns("id", "name", "publication_date").values(1L, "book 1", "2012-10-10").values(2L, "book 2", "2012-02-04").build());

		DbSetup dbSetup = new DbSetup(new DataSourceDestination(dataSource), inicialOps);

		dbSetupTracker.launchIfNecessary(dbSetup);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void findById() {
		dbSetupTracker.skipNextLaunch();

		List<Book> result = em.createNamedQuery(Book.FIND_BY_ID).setParameter("id", 1l).getResultList();

		assertEquals(1, result.size());
		assertEquals("book 1", result.get(0).getName());
		assertEquals(Long.valueOf(1), (Long) result.get(0).getId());
	}

	@Test
	public void findAll() {
		dbSetupTracker.skipNextLaunch();

		Insert.Builder data = Operations.insertInto("BOOK").withGeneratedValue("ID", ValueGenerators.sequence().startingAt(1000L).incrementingBy(10))
				.withGeneratedValue("name", ValueGenerators.stringSequence("bk")).columns("publication_date");
		for (int i = 0; i < 10; i++) {
			// deve haver uma chamada de values para cada valor q se quer
			// inserir
			data.values("2015-03-05");
		}

		Insert all = data.build();

		DbSetup dbSetup = new DbSetup(new DataSourceDestination(dataSource), all);
		dbSetup.launch();

		List<Book> result = em.createNamedQuery(Book.FIND_ALL).getResultList();

		assertEquals(12, result.size());

	}

	@Test
	public void findOneQueryDsl() {
		JPAQuery query = new JPAQuery(em);

		QBook book = QBook.book;
		Book b = query.from(book).where(book.name.eq("book 2")).uniqueResult(book);

		assertEquals(2l, b.getId().longValue());

	}

	@Test
	public void findAllQueryDSL() {
		dbSetupTracker.skipNextLaunch();

		Insert.Builder data = Operations.insertInto("BOOK").withGeneratedValue("ID", ValueGenerators.sequence().startingAt(1000L).incrementingBy(10))
				.withGeneratedValue("name", ValueGenerators.stringSequence("bk")).columns("publication_date");
		for (int i = 0; i < 10; i++) {
			// deve haver uma chamada de values para cada valor q se quer
			// inserir
			data.values("2015-03-05");
		}

		Insert all = data.build();

		DbSetup dbSetup = new DbSetup(new DataSourceDestination(dataSource), all);
		dbSetup.launch();

		JPAQuery query = new JPAQuery(em);

		StatelessSession ss = em.unwrap(StatelessSession.class);

	}

}
