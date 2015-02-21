package at.ac.tuwien.dsg.comot.m.core.spring;

import java.util.Properties;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import at.ac.tuwien.dsg.comot.m.cs.AppContextEps;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories("at.ac.tuwien.dsg.comot.m.core.dal")
@PropertySource({ "classpath:spring/properties/application.properties" })
@ComponentScan({ "at.ac.tuwien.dsg.comot.m.core" })
@Import({ AppContextEps.class })
@EnableAsync
public class AppContextCore {

	public static final Logger log = LoggerFactory.getLogger(AppContextCore.class);
	public static final String PACKAGE_ENTITYMANAGER = "at.ac.tuwien.dsg.comot.m.core.model";
	public static final String PERSISTENCE_NAME = "comot_persist";

	public static final String EMBEDDED_H2_DB = "EMBEDDED_H2_DB";
	public static final String INSERT_INIT_DATA = "INSERT_INIT_DATA";

	public static final String DATABASE_DRIVER = "db.driver";
	public static final String DATABASE_PASSWORD = "db.password";
	public static final String DATABASE_URL = "db.url";
	public static final String DATABASE_USERNAME = "db.username";

	public static final String HIBERNATE_DIALECT = "hibernate.dialect";
	public static final String HIBERNATE_SHOW_SQL = "hibernate.show_sql";
	public static final String HDM2DLL_AUTO = "hibernate.hbm2ddl.auto";

	public static final String DBUNIT_TESTDATA = "dbunit.testdata";

	@Resource
	public Environment env;
	@Autowired
	public DataSource dataSource;
	@Autowired
	public Properties jpaProperties;

	public AppContextCore() {
		super();
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(dataSource);
		em.setPackagesToScan(new String[] { PACKAGE_ENTITYMANAGER });
		em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
		em.setJpaProperties(jpaProperties);
		em.setPersistenceUnitName(PERSISTENCE_NAME);
		return em;
	}

	@Bean
	public PlatformTransactionManager transactionManager() {
		final JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
		transactionManager.setJpaDialect(new HibernateJpaDialect());
		transactionManager.setDataSource(dataSource);
		return transactionManager;
	}

	@Bean
	public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
		return new PersistenceExceptionTranslationPostProcessor();
	}

}
