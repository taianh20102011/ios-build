import Foundation
import UIKit
import MachO

public struct JailbreakStatus {
    public let isJailbroken: Bool
    public let title: String
    public let findings: [String]
    
    public init(isJailbroken: Bool, title: String, findings: [String]) {
        self.isJailbroken = isJailbroken
        self.title = title
        self.findings = findings
    }
}

public final class JailbreakDetector {
    
    private static let suspiciousPaths: [String] = [
        "/Applications/Cydia.app",
        "/Applications/Sileo.app",
        "/Applications/Zebra.app",
        "/Library/MobileSubstrate/MobileSubstrate.dylib",
        "/bin/bash",
        "/usr/sbin/sshd",
        "/etc/apt",
        "/usr/bin/ssh",
        "/private/var/lib/apt",
        "/private/var/lib/cydia",
        "/private/var/stash",
        "/usr/libexec/sftp-server",
        "/usr/bin/sftp",
        "/var/binpack",
        "/Applications/blackra1n.app",
        "/Library/PreferenceBundles/LibertyPref.bundle",
        "/Library/PreferenceBundles/ShadowPreferences.bundle"
    ]
    
    public static func checkJailbreak() -> JailbreakStatus {
        var findings: [String] = []
        
        // 1. Check known jailbreak filesystem paths
        for path in suspiciousPaths {
            if FileManager.default.fileExists(atPath: path) {
                findings.append("Phát hiện tệp/thư mục Jailbreak tại: \(path)")
            }
        }
        
        // 2. Check Sandbox Integrity (Write permission outside sandbox)
        let testPath = "/private/aegis_jailbreak_probe.txt"
        do {
            try "probe".write(toFile: testPath, atomically: true, encoding: .utf8)
            try? FileManager.default.removeItem(atPath: testPath)
            findings.append("Lỗ hổng Sandbox: Ứng dụng có thể ghi tệp vào phân vùng hệ thống /private")
        } catch {
            // Expected failure in jailed sandbox
        }
        
        // 3. Check loaded dynamic libraries (Frida, Substrate, CydiaSubstrate)
        let count = _dyld_image_count()
        for i in 0..<count {
            if let cName = _dyld_get_image_name(i) {
                let name = String(cString: cName).lowercased()
                if name.contains("frida") ||
                   name.contains("cydiasubstrate") ||
                   name.contains("mobilesubstrate") ||
                   name.contains("sslkillswitch") ||
                   name.contains("shadow.dylib") {
                    findings.append("Phát hiện thư viện hook/tiêm mã được nạp: \(name)")
                }
            }
        }
        
        // 4. Check symbolic links
        if let destination = try? FileManager.default.destinationOfSymbolicLink(atPath: "/Applications") {
            if destination.contains("var/stash") {
                findings.append("Thư mục /Applications đã bị chuyển hướng liên kết mềm (symlink)")
            }
        }
        
        let isCompromised = !findings.isEmpty
        return JailbreakStatus(
            isJailbroken: isCompromised,
            title: isCompromised ? "Thiết bị đã bị Jailbreak / Can thiệp hệ điều hành" : "Hệ thống nguyên bản (Chưa Jailbreak)",
            findings: findings.isEmpty ? ["Không phát hiện Cydia, Sileo, Frida, Substrate hoặc vi phạm sandbox."] : findings
        )
    }
}
