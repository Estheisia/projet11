package fr.uga.miashs.projetqcm;

import fr.uga.miashs.projetqcm.util.LocalDateConverter;
import org.apache.commons.beanutils.ConvertUtils;
import org.eclipse.microprofile.auth.LoginConfig;

import javax.annotation.security.DeclareRoles;
import javax.annotation.sql.DataSourceDefinition;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.time.LocalDate;


// from docuementation : https://docs.payara.fish/enterprise/docs/documentation/payara-server/jdbc/advanced-connection-pool-properties.html


@DataSourceDefinition(
        name = "java:app/projetqcm/projetqcmDS",
        className = "org.h2.jdbcx.JdbcDataSource",
        //url = "jdbc:h2:mem:test",
        url = "jdbc:h2:./qcmdb",//;TRACE_LEVEL_SYSTEM_OUT=3",
        initialPoolSize = 10,
        minPoolSize = 10,
        maxPoolSize = 200,
        //properties = {"fish.payara.is-connection-validation-required=true"}
        //databaseName = "cours3",
        user = "cours3Adm",
        password = "cours3Pass"
)

/**
 * Classe de configuration de l'application.
 * Notez qu'elle étend la classe javax.ws.rs.core.Application.
 * L'annotation @ApplicationPath("/data") indique que la racine du service web sera le chemin data.
 * Généralement la racine de l'application est : http://localhost:8080/.../data
 */
@ApplicationPath("/data")
@Singleton
@Transactional
@LoginConfig(authMethod = "MP-JWT")
@DeclareRoles({"user","teacher"})
public class ProjetQCMApplication extends Application {

    public ProjetQCMApplication() {
        ConvertUtils.register(new LocalDateConverter(), LocalDate.class);
    }

}
