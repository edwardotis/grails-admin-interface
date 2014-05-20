class GrailsAdminUrlMappings {
    static mappings = getDynamicUrlMapping()

    static getDynamicUrlMapping() {
        def baseUrl = grails.util.Holders.config.grails.plugin.admin.access_root
        if (!baseUrl) {
            baseUrl = "admin"
        } else if (baseUrl.startsWith("/")) {
            baseUrl = baseUrl.substring(1)
        }

        return {
            name grailsAdminDashboard: "/${baseUrl}/" { controller = "grailsAdminPlugin" ; action="dashboard" }

            name grailsAdminDelete: "/${baseUrl}/delete/$slug" { controller = "grailsAdminPlugin" ; action="delete" }
            name grailsAdminList: "/${baseUrl}/list/$slug/$page?" { controller = "grailsAdminPlugin" ; action="list" }
            name grailsAdminEdit: "/${baseUrl}/edit/$slug/$id" { controller = "grailsAdminPlugin" ; action=[GET:"edit", POST:"editAction"] }
            name grailsAdminAdd: "/${baseUrl}/add/$slug" { controller = "grailsAdminPlugin" ; action=[GET:"add", POST:"addAction"] }
            name grailsAdminSuccessEdit: "/${baseUrl}/success-edit/$slug" { controller = "grailsAdminPluginCallbackApi" ; action="successSave"}
            name grailsAdminSuccessList: "/${baseUrl}/success-list/$slug" { controller = "grailsAdminPluginCallbackApi" ; action="successList"}
            name grailsAdminSuccessNew: "/${baseUrl}/success-new/$slug" { controller = "grailsAdminPluginCallbackApi" ; action="successNew"}
            name grailsAdminSuccessDelete: "/${baseUrl}/success-delete/$slug" { controller = "grailsAdminPluginCallbackApi" ; action="successDelete"}

            // API
            name grailsAdminApiDashboard: "/${baseUrl}/api" { controller = "grailsAdminPluginApi" ; action = "listDomains" }
            name grailsAdminApiAction: "/${baseUrl}/api/$slug?/$id?" { controller = "grailsAdminPluginApi" ; action=[ GET:"getAdminAction", POST:"postAdminAction", DELETE:"deleteAdminAction", PUT:"putAdminAction"] }
            name grailsAdminRelatedApiAction: "/${baseUrl}/api/$slug/$id/$propertyName/$id2" { controller = "grailsAdminPluginApi" ; action=[ DELETE:"deleteRelatedAdminAction", PUT:"putRelatedAdminAction" ] }
        }
    }
}
