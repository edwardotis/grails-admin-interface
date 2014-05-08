<%@ page defaultCodec="HTML" %>

<!DOCTYPE html>
<html>
    <head>
        <title>${domain.className} - <g:message code="grailsAdminPlugin.list.title" /></title>
        <meta name="layout" content="grailsAdmin/main" />
    </head>
    <body>
        <div class="main-container container">
            <div class="row">
                <div class="col-md-7">
                    <ol class="breadcrumb">
                        <li><g:link mapping="grailsAdminDashboard"><g:message code='grailsAdminPlugin.dashboard.title'/></g:link></li>
                        <li class="active">${domain.className}</li>
                    </ol>
                </div>
                <div class="col-md-3 col-md-offset-2 object-nav">
                    <div class="btn-group">
                        <g:link mapping="grailsAdminAdd" params="[slug: domain.slug]" class="btn btn-default">
                            <span class="glyphicon glyphicon-plus"></span> <g:message code='grailsAdminPlugin.add.title' />
                        </g:link>
                    </div>
                </div>
            </div>
            <table class="table table-bordered">
                <thead>
                    <gap:listTitles className="${domain.domainClass.clazz.name}" />
                    <th class="list-actions-head">
                        <g:message code="grailsAdminPlugin.list.actions" />
                    </th>
                </thead>
                <tbody>
                <g:each in="${objs}">
                    <tr>
                        <gap:listLine object="${it}" />
                        <td class="list-actions">
                            <g:link mapping="grailsAdminEdit" params="[slug: domain.slug, id: it.id]" class="btn btn-default btn-sm">
                                <span class="glyphicon glyphicon-pencil"></span> <g:message code="grailsAdminPlugin.action.edit" />
                            </g:link>
                            <a data-id="${it.id}" data-toggle="modal" data-target="#confirm" class="btn btn-default btn-sm">
                                <span class="glyphicon glyphicon-trash"></span> <g:message code="grailsAdminPlugin.action.delete" />
                            </a>
                        </td>
                    </tr>
                </g:each>
                </tbody>
            </table>

            <g:if test="${totalPages > 1}">
            <ul class="pagination">
                <g:if test="${currentPage > 1}">
                <li>
                    <g:link mapping="grailsAdminList" params="[slug: domain.slug, page: currentPage - 1]">«</g:link>
                </li>
                </g:if>

                <g:each var="page" in="${ (1..totalPages) }">
                <li <g:if test="${page == currentPage}">class="active"</g:if>>
                    <g:link mapping="grailsAdminList" params="[slug: domain.slug, page: page]">${page}</g:link>
                </li>
                </g:each>

                <g:if test="${currentPage < totalPages}">
                <li>
                    <g:link mapping="grailsAdminList" params="[slug: domain.slug, page: currentPage + 1]">»</g:link>
                </li>
                </g:if>
            </ul>
            </g:if>
        </div>
        <g:render plugin="grailsAdmin" template="/grailsAdmin/includes/delete" />
    </body>
</html>
