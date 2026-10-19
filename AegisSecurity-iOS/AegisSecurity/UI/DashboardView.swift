import SwiftUI

public struct DashboardView: View {
    @Binding var jailbreakStatus: JailbreakStatus
    @Binding var isDebuggerAttached: Bool
    @Binding var hasInjectedDyld: Bool
    @Binding var isScanning: Bool
    let onScan: () -> Void
    
    private var securityScore: Int {
        var score = 100
        if jailbreakStatus.isJailbroken { score -= 45 }
        if isDebuggerAttached { score -= 30 }
        if hasInjectedDyld { score -= 25 }
        return max(0, score)
    }
    
    private var scoreColor: Color {
        if securityScore >= 80 { return CyberTheme.safe }
        if securityScore >= 50 { return CyberTheme.warning }
        return CyberTheme.danger
    }
    
    public var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                // Header
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("AEGIS SHIELD iOS")
                            .font(.system(size: 20, weight: .black))
                            .foregroundColor(CyberTheme.primary)
                        Text("Apple Secure Enclave & Integrity Guard")
                            .font(.system(size: 12))
                            .foregroundColor(CyberTheme.textSecondary)
                    }
                    Spacer()
                    HStack(spacing: 6) {
                        Circle()
                            .fill(isScanning ? CyberTheme.warning : CyberTheme.safe)
                            .frame(width: 8, height: 8)
                        Text(isScanning ? "SCANNING" : "ACTIVE")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(CyberTheme.textPrimary)
                    }
                    .padding(.horizontal, 10)
                    .padding(.vertical, 6)
                    .background(CyberTheme.card)
                    .cornerRadius(12)
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(CyberTheme.border, lineWidth: 1))
                }
                .padding(.horizontal)
                
                // Big Circular Shield Indicator
                ZStack {
                    Circle()
                        .stroke(CyberTheme.card, lineWidth: 10)
                        .frame(width: 170, height: 170)
                    Circle()
                        .trim(from: 0.0, to: CGFloat(securityScore) / 100.0)
                        .stroke(scoreColor, style: StrokeStyle(lineWidth: 10, lineCap: .round))
                        .rotationEffect(.degrees(-90))
                        .frame(width: 170, height: 170)
                        .animation(.easeInOut(duration: 0.8), value: securityScore)
                    
                    VStack(spacing: 4) {
                        Image(systemName: securityScore >= 80 ? "shield.checkmark.fill" : "exclamationmark.shield.fill")
                            .font(.system(size: 38))
                            .foregroundColor(scoreColor)
                        Text("\(securityScore)%")
                            .font(.system(size: 32, weight: .black))
                            .foregroundColor(CyberTheme.textPrimary)
                        Text(securityScore >= 80 ? "BẢO VỆ TỐI ƯU" : "CẢNH BÁO")
                            .font(.system(size: 10, weight: .bold))
                            .foregroundColor(scoreColor)
                    }
                }
                .padding(.vertical, 10)
                
                // Deep Scan Button
                Button(action: onScan) {
                    HStack {
                        Image(systemName: "radar")
                        Text(isScanning ? "ĐANG QUÉT HỆ THỐNG..." : "QUÉT SÂU TOÀN DIỆN iOS")
                            .font(.system(size: 14, weight: .bold))
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 50)
                    .background(CyberTheme.primary)
                    .foregroundColor(.black)
                    .cornerRadius(14)
                }
                .disabled(isScanning)
                .padding(.horizontal)
                
                // Status Items
                VStack(spacing: 10) {
                    StatusRowCard(
                        title: "Kiểm tra Jailbreak & File Phân vùng",
                        subtitle: jailbreakStatus.title,
                        isSafe: !jailbreakStatus.isJailbroken,
                        icon: "lock.shield.fill"
                    )
                    
                    StatusRowCard(
                        title: "Trình Gỡ lỗi & Can thiệp (Anti-Debug)",
                        subtitle: isDebuggerAttached ? "Phát hiện Tracing/Debugger gắn vào app" : "Không có debugger trái phép (An toàn)",
                        isSafe: !isDebuggerAttached,
                        icon: "ladybug.fill"
                    )
                    
                    StatusRowCard(
                        title: "Thư viện Tiêm mã (Frida / Dylib Injection)",
                        subtitle: hasInjectedDyld ? "Phát hiện DYLD_INSERT_LIBRARIES" : "Không phát hiện tiêm mã động",
                        isSafe: !hasInjectedDyld,
                        icon: "memorychip.fill"
                    )
                    
                    StatusRowCard(
                        title: "Két sắt Secure Enclave",
                        subtitle: "Mã hóa cấp phần cứng Apple Silicon",
                        isSafe: true,
                        icon: "key.fill"
                    )
                }
                .padding(.horizontal)
            }
            .padding(.vertical)
        }
        .background(CyberTheme.background.ignoresSafeArea())
    }
}

struct StatusRowCard: View {
    let title: String
    let subtitle: String
    let isSafe: Bool
    let icon: String
    
    var body: some View {
        HStack(spacing: 12) {
            ZStack {
                RoundedRectangle(cornerRadius: 10)
                    .fill(isSafe ? CyberTheme.safe.opacity(0.15) : CyberTheme.danger.opacity(0.15))
                    .frame(width: 40, height: 40)
                Image(systemName: icon)
                    .foregroundColor(isSafe ? CyberTheme.safe : CyberTheme.danger)
                    .font(.system(size: 18))
            }
            
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(CyberTheme.textPrimary)
                Text(subtitle)
                    .font(.system(size: 12))
                    .foregroundColor(isSafe ? CyberTheme.textSecondary : CyberTheme.danger)
                    .lineLimit(1)
            }
            
            Spacer()
            
            Image(systemName: isSafe ? "checkmark.circle.fill" : "xmark.circle.fill")
                .foregroundColor(isSafe ? CyberTheme.safe : CyberTheme.danger)
        }
        .padding(12)
        .background(CyberTheme.card)
        .cornerRadius(12)
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(isSafe ? CyberTheme.border : CyberTheme.danger.opacity(0.4), lineWidth: 1))
    }
}
