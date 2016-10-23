package guru.nidi.mapvisualizer

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.handler.AllowSymLinkAliasChecker
import org.eclipse.jetty.server.handler.ContextHandler
import org.eclipse.jetty.server.handler.ResourceHandler
import org.eclipse.jetty.util.resource.Resource
import org.eclipse.jetty.util.resource.ResourceCollection

object FileServer {
    @JvmStatic
    fun main(args: Array<String>) {
        val server = Server()
        val connector = ServerConnector(server)
        connector.port = 8080
        server.addConnector(connector)

        val context = ContextHandler()
        context.addAliasCheck(AllowSymLinkAliasChecker())
        server.handler = context

        val resources = ResourceHandler()
        resources.isDirectoriesListed = true
        resources.baseResource = ResourceCollection(
                Resource.newResource(Tools.dir),
                Resource.newResource("src/main/resources"),
                Resource.newClassPathResource("/"))
        context.handler = resources

        server.start()
        server.join()
    }
}
