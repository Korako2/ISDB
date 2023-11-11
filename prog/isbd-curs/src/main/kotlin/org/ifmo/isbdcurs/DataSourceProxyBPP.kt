package org.ifmo.isbdcurs


import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.springframework.aop.framework.ProxyFactory
import org.springframework.beans.BeansException
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.util.ReflectionUtils
import javax.sql.DataSource


@Component
@Profile("batchinserts")
class DatasourceProxyBeanPostProcessor : BeanPostProcessor {
    @Throws(BeansException::class)
    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        return bean
    }

    @Throws(BeansException::class)
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        if (bean is DataSource) {
            val factory = ProxyFactory(bean)
            factory.isProxyTargetClass = true
            factory.addAdvice(ProxyDataSourceInterceptor(bean))
            return factory.proxy
        }
        return bean
    }

    private class ProxyDataSourceInterceptor(dataSource: DataSource?) : MethodInterceptor {
        private val dataSource: DataSource

        init {
            this.dataSource =
                ProxyDataSourceBuilder.create(dataSource).name("Batch-Insert-Logger").asJson().countQuery()
                    .logQueryToSysOut().build()
        }

        @Throws(Throwable::class)
        override fun invoke(invocation: MethodInvocation): Any? {
            val proxyMethod = ReflectionUtils.findMethod(dataSource.javaClass, invocation.method.name)
            return if (proxyMethod != null) {
                proxyMethod.invoke(dataSource, *invocation.arguments)
            } else invocation.proceed()
        }
    }
}