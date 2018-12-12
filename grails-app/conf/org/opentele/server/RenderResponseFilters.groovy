package org.opentele.server

class RenderResponseFilters {

    def responsePipelineService

    def filters = {
        renderResponse(controller: '*', action: '*') {
            after = { Map model ->
                if (model?.resourceType && model?.resource != null) {
                    responsePipelineService.invokeResponseListeners(model.resourceType, model.resource)

                    def statusCode = model.status ? model.status : 200
                    render(status: statusCode, contentType: 'application/json') {return model.resource}
                    responsePipelineService.clearResponseListeners()
                }
            }
        }
    }
}
