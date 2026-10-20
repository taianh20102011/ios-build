import Foundation
import Network
import CFNetwork

public struct IOSNetworkStatus {
    public let connectionType: String
    public let isVpnActive: Bool
    public let isProxyConfigured: Bool
    public let warnings: [String]
    public let tips: [String]
    
    public init(connectionType: String, isVpnActive: Bool, isProxyConfigured: Bool, warnings: [String], tips: [String]) {
        self.connectionType = connectionType
        self.isVpnActive = isVpnActive
        self.isProxyConfigured = isProxyConfigured
        self.warnings = warnings
        self.tips = tips
    }
}

public final class IOSNetworkInspector: ObservableObject {
    private let monitor = NWPathMonitor()
    private let queue = DispatchQueue(label: "com.aegis.networkmonitor")
    
    @Published public var currentStatus: IOSNetworkStatus = IOSNetworkStatus(
        connectionType: "Đang kiểm tra...",
        isVpnActive: false,
        isProxyConfigured: false,
        warnings: [],
        tips: []
    )
    
    public init() {
        startMonitoring()
    }
    
    public func startMonitoring() {
        monitor.pathUpdateHandler = { [weak self] path in
            DispatchQueue.main.async {
                self?.analyzePath(path)
            }
        }
        monitor.start(queue: queue)
    }
    
    private func analyzePath(_ path: NWPath) {
        var connType = "Không có kết nối"
        let isWifi = path.usesInterfaceType(.wifi)
        let isCellular = path.usesInterfaceType(.cellular)
        let isWired = path.usesInterfaceType(.wiredEthernet)
        
        if isWifi { connType = "Mạng Wi-Fi" }
        else if isCellular { connType = "Dữ liệu di động (4G/5G)" }
        else if isWired { connType = "Kết nối dây (Ethernet)" }
        
        // Proxy Check
        var isProxy = false
        if let proxySettings = CFNetworkCopySystemProxySettings()?.takeRetainedValue() as? [String: Any],
           let httpProxy = proxySettings["HTTPProxy"] as? String, !httpProxy.isEmpty {
            isProxy = true
        }
        
        // VPN Detection heuristic
        var isVpn = false
        if let scoped = path.availableInterfaces.first(where: { $0.name.contains("utun") || $0.name.contains("ppp") || $0.name.contains("ipsec") }) {
            isVpn = true
        }
        
        var warnings: [String] = []
        var tips: [String] = []
        
        if isProxy {
            warnings.append("Phát hiện Proxy đang định tuyến dữ liệu. Lưu lượng có thể bị theo dõi.")
        }
        
        if isWifi && !isVpn {
            tips.append("Nên kích hoạt iCloud Private Relay hoặc VPN khi truy cập Wi-Fi công cộng.")
        } else if isVpn {
            tips.append("Đường truyền mạng được bảo vệ và mã hóa qua kênh VPN an toàn.")
        }
        
        self.currentStatus = IOSNetworkStatus(
            connectionType: connType,
            isVpnActive: isVpn,
            isProxyConfigured: isProxy,
            warnings: warnings,
            tips: tips
        )
    }
}
