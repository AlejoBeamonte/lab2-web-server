package es.unizar.webeng.lab2

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.view.json.JacksonJsonView
import org.springframework.web.servlet.view.xml.JacksonXmlView
import org.thymeleaf.spring6.view.ThymeleafViewResolver

const val TIME_MODEL_ATTRIBUTE = "timeSnapshot"

@Configuration(proxyBeanMethods = false)
class TimeViewConfiguration(
    private val thymeleafViewResolver: ThymeleafViewResolver,
) : WebMvcConfigurer {
    override fun configureViewResolvers(registry: ViewResolverRegistry) {
        registry.enableContentNegotiation(
            true,
            JacksonJsonView().apply {
                setModelKey(TIME_MODEL_ATTRIBUTE)
                setExtractValueFromSingleKeyModel(true)
            },
            JacksonXmlView().apply { setModelKey(TIME_MODEL_ATTRIBUTE) },
        )
        registry.viewResolver(thymeleafViewResolver)
    }
}
