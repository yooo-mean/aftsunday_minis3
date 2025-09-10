package aftsundayServer.aftsunday_minis3.config

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import kotlin.jvm.Throws

@Component
class ApiKeyFilter(
    private val props: AppProperties
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val uri = request.requestURI
        val method = request.method
        //읽기 계열 및 문서/헬스체크는 통과
        val readOnly = method == HttpMethod.GET.name() || method == HttpMethod.HEAD.name()
        val isDocs = uri.startsWith("/swagger-ui") || uri.startsWith("/v3/api-docs")
        val isHealth = uri.startsWith("/actuator")
        val isPresign = uri.startsWith("/presign")      //프리사인 생성은 쓰기 취급 -> 보호하려면 false로
        return readOnly || isDocs || isHealth
    }

    @Throws(ServletException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val apiKey = request.getHeader("X-API-KEY")
        if(apiKey == null || apiKey != props.apiKey) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.writer.write("{\"error\":\"missing_or_invalid_api_key\"}")
            return
        }
        FilterChain.doFilter(request, response)
    }
}