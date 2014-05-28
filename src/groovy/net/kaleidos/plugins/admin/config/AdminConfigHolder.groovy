package net.kaleidos.plugins.admin.config

import groovy.util.logging.Log4j
import java.util.regex.Pattern

import grails.util.Holders
import grails.util.Environment

import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.web.context.WebApplicationContext
import org.springframework.util.ClassUtils

import org.codehaus.groovy.grails.validation.ConstrainedProperty;

import net.kaleidos.plugins.admin.widget.Widget
import net.kaleidos.plugins.admin.DomainInspector

@Log4j
class AdminConfigHolder {
    Map<String, DomainConfig> domains = [:]

    void initialize() {
        _mergeConfiguration()
        _configureAdminRole()
        _configureDomains()
    }

    public List getDomainClasses() {
        return this.domains.keySet() as List
    }

    public List<String> getDomainNames() {
        return this.domainClasses.collect{ this.domains[it].className }
    }

    public List<String> getSlugDomainNames() {
        return this.domainClasses.collect{ this.domains[it].slug }
    }

    public DomainConfig getDomainConfig(String objClass) {
        try {
            return getDomainConfig(Class.forName(objClass, true, Thread.currentThread().contextClassLoader))
        } catch (ClassNotFoundException e) {
            // Sometimes Domain classes throws a ClassNotFoundException. We shoudl fall-back to the grails implementation
            return Holders.grailsApplication.getClassForName(objClass)
        }
    }

    public DomainConfig getDomainConfig(Object object) {
        if (!object) {
            return null
        }
        def clazz = ClassUtils.getUserClass(object?.getClass())
        return getDomainConfig(clazz)
    }

    public DomainConfig getDomainConfig(Class objClass) {
        if (!objClass || Object.class == objClass) {
            return null
        }
        def config = this.domains[objClass.name]

        if (!config) {
            config = _getParentDomainConfig(objClass)
        }

        if (!config && DomainInspector.isDomain(objClass) && Holders.config.grails.plugin.admin.allowDefaultConfig) {
            config = new DomainConfig(objClass)
        }

        return config
    }

    public DomainConfig _getParentDomainConfig(Class clazz) {
        def objClass = clazz.getSuperclass()
        if (!objClass || Object.class == objClass) {
            return null
        }

        def config = this.domains[objClass.name]
        if (!config) {
            config = _getParentDomainConfig(objClass)
        }

        return config
    }

    public DomainConfig getDomainConfigForProperty(Object object, String property) {
        def clazz = ClassUtils.getUserClass(object.getClass())
        return getDomainConfigForProperty(clazz, property)
    }

    public DomainConfig getDomainConfigForProperty(Class objClass, String property) {
        def inspector = new DomainInspector(objClass)
        return getDomainConfig(inspector.getPropertyClass(property))
    }

    public DomainConfig getDomainConfigBySlug(String slug) {
        def config = this.domains.find { it.value.slug == slug }?.value

        if (!config) {
            def clazz = DomainInspector.getClassWithSlug(slug)
            if (clazz) {
                config = new DomainConfig(clazz)
            }
        }

        return config
    }

    def _mergeConfiguration() {
        def userConfiguration = Holders.config.grails.plugin.admin
        def configSlurper = new ConfigSlurper(Environment.getCurrent().getName())
        def defaultConfiguration = configSlurper.parse(Holders.grailsApplication.classLoader.loadClass("GrailsAdminDefaultConfig"))
        Holders.config.grails.admin = _mergeConfigObjects(defaultConfiguration.defaultAdminConfig, userConfiguration)
    }

    def _mergeConfigObjects(ConfigObject confDefault, ConfigObject confUser) {
        def config = new ConfigObject()
        if (confUser == null) {
            if (confDefault != null) {
                config.putAll(confDefault)
            }
        }
        else {
            if (confDefault == null) {
                config.putAll(confUser)
            } else {
                config.putAll(confDefault)
                config.putAll(confDefault.merge(confUser))
            }
        }
        return config
    }

    def _configureDomains() {
        def domainList = Holders.config.grails.plugin.admin.domains
        if (!domainList) {
            return;
        }
        log.debug "Configuring domain classes"

        this.domains = [:]
        domainList.each { name ->
            def inspector = DomainInspector.find(name)

            if (!inspector) {
                throw new RuntimeException("Configured class ${name} is not a domain class")
            }

            def domainConfig = Holders.config.grails.plugin.admin.domain."${inspector.className}"

            if (domainConfig && domainConfig instanceof Closure) {
                def dsl = new DomainConfigurationDsl(inspector.clazz, domainConfig)
                domains[name] = dsl.execute()
            } else if (domainConfig && domainConfig instanceof String) {
                def clazz = Class.forName(domainConfig)
                if (!clazz.metaClass.respondsTo(clazz, "getOptions")) {
                    throw new RuntimeException("Class $domainConfig doesn't have a static attribute 'options'")
                }
                def dsl = new DomainConfigurationDsl(inspector.clazz, clazz.options)
                domains[name] = dsl.execute()
            } else {
                domains[name] = new DomainConfig(inspector.clazz)
            }
        }
        log.debug "DOMAIN: ${this.domains}"
    }

    def _configureAdminRole() {
        if (!_configureAdminRoleSecurity1() && !_configureAdminRoleSecurity2()) {
            log.error "No configured Spring Security"
            if (Environment.current == Environment.PRODUCTION && Holders.config.grails.plugin.admin.security.forbidUnsecureProduction) {
                String message = "You have not configured Spring Security. You can deactivate this feature setting 'grails.plugin.admin.security.forbidUnsecureProduction=false' in your configuration file"
                log.error message
                System.err.println message
                throw new RuntimeException(message)
            }
        }
    }

    boolean _configureAdminRoleSecurity1() {
        try {
            def role = Holders.config.grails.plugin.admin.security.role?:"ROLE_ADMIN"

            def clazz =  Class.forName("org.springframework.security.access.SecurityConfig")
            def constructor = clazz.getConstructor(String.class)
            def newConfig = constructor.&newInstance

            def objectDefinitionSource = Holders.grailsApplication.mainContext.getBean("objectDefinitionSource")
            objectDefinitionSource.storeMapping("/grailsadminpluginui/**", [newConfig(role)] as Set)
            objectDefinitionSource.storeMapping("/grailsadminpluginapi/**", [newConfig(role)] as Set)
            objectDefinitionSource.storeMapping("/grailsadminplugincallbackapi/**", [newConfig(role)] as Set)
        } catch (Throwable e) {
            return false
        }
    }

    boolean _configureAdminRoleSecurity2() {
        try {
            def role = Holders.config.grails.plugin.admin.security.role?:"ROLE_ADMIN"

            // We use reflection so it doesn't have a compile-time dependency
            def clazz = Class.forName("grails.plugin.springsecurity.InterceptedUrl")
            def httpMethodClass = Class.forName("org.springframework.http.HttpMethod")
            def constructor = clazz.getConstructor(String.class, Collection.class, httpMethodClass)
            def newUrl = constructor.&newInstance

            def objectDefinitionSource = Holders.grailsApplication.mainContext.getBean("objectDefinitionSource")
            objectDefinitionSource.compiled << newUrl("/grailsadminpluginui", [role], null)
            objectDefinitionSource.compiled << newUrl("/grailsadminpluginui.*", [role], null)
            objectDefinitionSource.compiled << newUrl("/grailsadminpluginui/**", [role], null)
            objectDefinitionSource.compiled << newUrl("/grailsadminpluginapi", [role], null)
            objectDefinitionSource.compiled << newUrl("/grailsadminpluginapi.*", [role], null)
            objectDefinitionSource.compiled << newUrl("/grailsadminpluginapi/**", [role], null)
            objectDefinitionSource.compiled << newUrl("/grailsadminplugincallbackapi", [role], null)
            objectDefinitionSource.compiled << newUrl("/grailsadminplugincallbackapi.*", [role], null)
            objectDefinitionSource.compiled << newUrl("/grailsadminplugincallbackapi/**", [role], null)
        } catch (Throwable e) {
            return false
        }
    }
}
