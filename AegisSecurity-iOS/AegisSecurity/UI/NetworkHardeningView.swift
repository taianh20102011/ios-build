import SwiftUI

public struct NetworkHardeningView: View {
    @ObservedObject var networkInspector: IOSNetworkInspector
    
    public var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text("AN NINH MẠNG & GIA CỐ iOS")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(CyberTheme.primary)
                Text("Kiểm tra đường hầm kết nối, proxy và thiết lập an toàn mạng")
                    .font(.system(size: 12))
                    .foregroundColor(CyberTheme.textSecondary)
                
                // Network Card
                VStack(alignment: .leading, spacing: 12) {
                    HStack {
                        Image(systemName: "wifi")
                            .foregroundColor(CyberTheme.primary)
                            .font(.system(size: 22))
                        VStack(alignment: .leading, spacing: 2) {
                            Text(networkInspector.currentStatus.connectionType)
                                .font(.system(size: 15, weight: .bold))
                                .foregroundColor(CyberTheme.textPrimary)
                            Text(networkInspector.currentStatus.isVpnActive ? "Kênh truyền bảo vệ bởi VPN" : "Không sử dụng VPN")
                                .font(.system(size: 12))
                                .foregroundColor(CyberTheme.textSecondary)
                        }
                    }
                    
                    Divider().background(CyberTheme.border)
                    
                    HStack {
                        Text("Trạng thái Proxy:")
                            .font(.system(size: 12))
                            .foregroundColor(CyberTheme.textSecondary)
                        Spacer()
                        Text(networkInspector.currentStatus.isProxyConfigured ? "CẢNH BÁO: CÓ PROXY" : "Không có Proxy (An toàn)")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(networkInspector.currentStatus.isProxyConfigured ? CyberTheme.danger : CyberTheme.safe)
                    }
                    
                    HStack {
                        Text("Đường truyền mã hóa (VPN):")
                            .font(.system(size: 12))
                            .foregroundColor(CyberTheme.textSecondary)
                        Spacer()
                        Text(networkInspector.currentStatus.isVpnActive ? "Đang bật" : "Chưa bật")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(networkInspector.currentStatus.isVpnActive ? CyberTheme.safe : CyberTheme.textSecondary)
                    }
                    
                    ForEach(networkInspector.currentStatus.warnings, id: \.self) { w in
                        Text("⚠ \(w)")
                            .font(.system(size: 11))
                            .foregroundColor(CyberTheme.danger)
                    }
                    
                    ForEach(networkInspector.currentStatus.tips, id: \.self) { t in
                        Text("💡 \(t)")
                            .font(.system(size: 11))
                            .foregroundColor(CyberTheme.primary)
                    }
                }
                .padding(14)
                .background(CyberTheme.card)
                .cornerRadius(12)
                .overlay(RoundedRectangle(cornerRadius: 12).stroke(CyberTheme.border, lineWidth: 1))
                
                // iOS Best Practices Card
                VStack(alignment: .leading, spacing: 10) {
                    Text("GIA CỐ BẢO MẬT HỆ ĐIỀU HÀNH (BEST PRACTICES)")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(CyberTheme.textSecondary)
                    
                    HardeningRow(title: "Face ID / Touch ID & Mật mã 6 số", desc: "Luôn bật xác thực sinh trắc học để bảo vệ Secure Enclave.", isGood: true)
                    HardeningRow(title: "Chống Ghi màn hình (Screen Shield)", desc: "Aegis kích hoạt lớp bảo vệ chống lộ mã PIN khi quay phim màn hình.", isGood: true)
                    HardeningRow(title: "Chế độ Phong tỏa (Lockdown Mode)", desc: "Kích hoạt trong Cài đặt iOS nếu bạn có nguy cơ bị tấn công bởi phần mềm gián điệp tinh vi (Pegasus).", isGood: true)
                }
                .padding(14)
                .background(CyberTheme.card)
                .cornerRadius(12)
                .overlay(RoundedRectangle(cornerRadius: 12).stroke(CyberTheme.border, lineWidth: 1))
                
                Spacer()
            }
            .padding()
        }
        .background(CyberTheme.background.ignoresSafeArea())
    }
}

struct HardeningRow: View {
    let title: String
    let desc: String
    let isGood: Bool
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack {
                Image(systemName: "checkmark.seal.fill")
                    .foregroundColor(CyberTheme.safe)
                    .font(.system(size: 14))
                Text(title)
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundColor(CyberTheme.textPrimary)
            }
            Text(desc)
                .font(.system(size: 11))
                .foregroundColor(CyberTheme.textSecondary)
        }
        .padding(.vertical, 4)
    }
}
