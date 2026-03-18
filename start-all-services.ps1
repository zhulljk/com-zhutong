# 服务启动脚本
# 用于快速启动所有微服务

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   微服务启动脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查 Consul 是否运行
Write-Host "[1/6] 检查 Consul 服务..." -ForegroundColor Yellow
$consulRunning = Get-Process | Where-Object {$_.ProcessName -eq "consul"}
if (-not $consulRunning) {
    Write-Host "Consul 未运行！请先启动 Consul:" -ForegroundColor Red
    Write-Host "  consul agent -dev -ui" -ForegroundColor Yellow
    Write-Host "或使用 Docker:" -ForegroundColor Yellow
    Write-Host "  docker run -d -p 8500:8500 -p 8600:8600/udp consul:latest" -ForegroundColor Yellow
    exit 1
}
Write-Host "✓ Consul 正在运行" -ForegroundColor Green
Write-Host ""

# 启动 Center Server
Write-Host "[2/6] 启动 Center Server..." -ForegroundColor Yellow
Write-Host "  服务名：center-server" -ForegroundColor Cyan
Write-Host "  端口：8090" -ForegroundColor Cyan
Write-Host "  访问地址：" -ForegroundColor Cyan
Write-Host "    - Actuator: http://localhost:8090/actuator" -ForegroundColor Cyan
Write-Host "    - Consul: http://localhost:8500/ui/dc1/services" -ForegroundColor Cyan
Write-Host ""

# 启动 User Service
Write-Host "[3/6] 启动 User Service..." -ForegroundColor Yellow
Write-Host "  服务名：user" -ForegroundColor Cyan
Write-Host "  端口：8080" -ForegroundColor Cyan
Write-Host "  访问地址：" -ForegroundColor Cyan
Write-Host "    - Actuator: http://localhost:8080/actuator" -ForegroundColor Cyan
Write-Host "    - API: http://localhost:8080/api" -ForegroundColor Cyan
Write-Host ""

# 启动 Menu Service
Write-Host "[4/6] 启动 Menu Service..." -ForegroundColor Yellow
Write-Host "  服务名：menu" -ForegroundColor Cyan
Write-Host "  端口：8081" -ForegroundColor Cyan
Write-Host "  访问地址：" -ForegroundColor Cyan
Write-Host "    - Actuator: http://localhost:8081/actuator" -ForegroundColor Cyan
Write-Host "    - API: http://localhost:8081/api" -ForegroundColor Cyan
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   启动命令" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "请在新窗口中执行以下命令：" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. 启动 Center Server:" -ForegroundColor Green
Write-Host "   cd center-server" -ForegroundColor White
Write-Host "   mvn spring-boot:run" -ForegroundColor White
Write-Host ""
Write-Host "2. 启动 User Service:" -ForegroundColor Green
Write-Host "   cd user" -ForegroundColor White
Write-Host "   mvn spring-boot:run" -ForegroundColor White
Write-Host ""
Write-Host "3. 启动 Menu Service:" -ForegroundColor Green
Write-Host "   cd menu" -ForegroundColor White
Write-Host "   mvn spring-boot:run" -ForegroundColor White
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   验证服务注册" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. 访问 Consul UI 查看所有注册的服务:" -ForegroundColor Yellow
Write-Host "   http://localhost:8500/ui/dc1/services" -ForegroundColor Cyan
Write-Host ""
Write-Host "2. 使用 curl 命令查看服务:" -ForegroundColor Yellow
Write-Host "   curl http://localhost:8500/v1/catalog/services" -ForegroundColor Cyan
Write-Host ""
Write-Host "3. 查看各个服务的健康状态:" -ForegroundColor Yellow
Write-Host "   curl http://localhost:8090/actuator/health" -ForegroundColor Cyan
Write-Host "   curl http://localhost:8080/actuator/health" -ForegroundColor Cyan
Write-Host "   curl http://localhost:8081/actuator/health" -ForegroundColor Cyan
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   服务间调用示例" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "使用服务名进行调用（无需硬编码地址）:" -ForegroundColor Yellow
Write-Host ""
Write-Host "Java 代码示例:" -ForegroundColor Green
Write-Host "@Autowired" -ForegroundColor White
Write-Host "private RestTemplate restTemplate;" -ForegroundColor White
Write-Host ""
Write-Host "// 调用 User 服务" -ForegroundColor Gray
Write-Host "restTemplate.getForObject(" -ForegroundColor White
Write-Host "    `"http://user/api/users/1`"," -ForegroundColor White
Write-Host "    User.class" -ForegroundColor White
Write-Host ");" -ForegroundColor White
Write-Host ""
Write-Host "// 调用 Menu 服务" -ForegroundColor Gray
Write-Host "restTemplate.getForObject(" -ForegroundColor White
Write-Host "    `"http://menu/api/menus/1`"," -ForegroundColor White
Write-Host "    Menu.class" -ForegroundColor White
Write-Host ");" -ForegroundColor White
Write-Host ""

Write-Host "提示：详细文档请参阅 SERVICES-REGISTRATION.md" -ForegroundColor Yellow
Write-Host ""
