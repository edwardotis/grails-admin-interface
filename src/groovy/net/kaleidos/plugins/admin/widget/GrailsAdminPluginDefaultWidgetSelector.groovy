package net.kaleidos.plugins.admin.widget

import org.codehaus.groovy.grails.validation.ConstrainedProperty
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClassProperty
import org.codehaus.groovy.grails.validation.InListConstraint
import org.codehaus.groovy.grails.validation.UrlConstraint
import org.codehaus.groovy.grails.validation.EmailConstraint
import net.kaleidos.plugins.admin.widget.relation.RelationSelectMultipleWidget
import net.kaleidos.plugins.admin.widget.relation.RelationTableWidget
import net.kaleidos.plugins.admin.widget.relation.RelationPopupOneWidget
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import net.kaleidos.plugins.admin.DomainInspector


class GrailsAdminPluginDefaultWidgetSelector {

    static Widget getDefaultWidgetForProperty(Class clazz, String propertyName){
        def widget
        def inspector = new DomainInspector(clazz)
        def constraints = inspector.getPropertyConstraints(propertyName)
        def constraintsClasses = constraints*.class
        def type = inspector.getPropertyClass(propertyName)

        if (InListConstraint.class in constraintsClasses){
            widget = new SelectWidget()
        } else {
            switch ( type ) {
                case [Byte, Short, Integer, Long, Float, Double]:
                    widget = new NumberInputWidget()
                    break
                case [Character, String]:
                    if (EmailConstraint.class in constraintsClasses) {
                        widget = new EmailInputWidget()
                    } else if (UrlConstraint.class in constraintsClasses){
                        widget = new UrlInputWidget()
                    } else {
                        widget = new TextInputWidget()
                    }
                    break
                case Date:
                    widget = new DateInputWidget()
                    break
                case Boolean:
                    widget = new CheckboxInputWidget()
                    break
                case File:
                    widget = new LabelWidget()
                    break
                case Collection:
                    if (inspector.isAssociation(propertyName)) {
                        widget = new RelationTableWidget()
                    } else {
                        widget = new LabelWidget()
                    }
                    break
                default:
                    //It is another domain class?
                    //TODO: Change to use inspector
                    def domain
                    try {
                        domain = new DefaultGrailsDomainClass(type)
                    } catch (Exception e){}
                    if (domain) {
                        widget = new RelationPopupOneWidget()
                        //widget.internalAttrs["relatedDomainClass"] = domain
                    } else {
                        widget = new TextInputWidget()
                    }
            }
        }
        return widget
    }

}
