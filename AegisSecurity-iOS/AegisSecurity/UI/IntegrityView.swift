import SwiftUI

public struct IntegrityView: View {
    let jailbreakStatus: JailbreakStatus
    let isDebuggerAttached: Bool
    let hasInjectedDyld: Bool
    
    public var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text("KIỂM TRA NGUYÊN BẢN HỆ THỐNG")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(CyberTheme.primary)
                Text("Phát hiện Jailbreak, vi phạm Sandbox và thư viện can thiệp ngầm")
                    .font(.system(size: 12))
                    .foregroundColor(CyberTheme.textSecondary)
                
                // Jailbreak Card
                VStack(alignment: .leading, spacing: 10) {
                    HStack {
                        Image(systemName: jailbreakStatus.isJailbroken ? "exclamationmark.triangle.fill" : "checkmark.shield.fill")
                            .foregroundColor(jailbreakStatus.isJailbroken ? CyberTheme.danger : CyberTheme.safe)
                        Text(jailbreakStatus.title)
                            .font(.system(size: 15, weight: .bold))
                            .foregroundColor(CyberTheme.textPrimary)
                    }
                    Divider().background(CyberTheme.border)
                    
                    Text("CHI TIẾT KIỂM TRA:")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(CyberTheme.textSecondary)
                    
                    ForEach(jailbreakStatus.findings, id: \.self) { finding in
                        HStack(alignment: .top, spacing: 6) {
                            Text("•")
                                .foregroundColor(jailbreakStatus.isJailbroken ? CyberTheme.danger : CyberTheme.safe)
                            Text(finding)
                                .font(.system(size: 12))
                                .foregroundColor(CyberTheme.textPrimary)
                        }
                    }
                }
                .padding(14)
                .background(CyberTheme.card)
                .cornerRadius(12)
                .overlay(RoundedRectangle(cornerRadius: 12).stroke(jailbreakStatus.isJailbroken ? CyberTheme.danger.opacity(0.5) : CyberTheme.border, lineWidth: 1))
                
                // Anti-Debug Details Card
                VStack(alignment: .leading, spacing: 10) {
                    HStack {
                        Image(systemName: isDebuggerAttached ? "ladybug.fill" : "shield.lefthalf.filled")
                            .foregroundColor(isDebuggerAttached ? CyberTheme.danger : CyberTheme.safe)
                        Text("Trạng thái Debugger (Ptrace / Sysctl)")
                            .font(.system(size: 15, weight: .bold))
                            .foregroundColor(CyberTheme.textPrimary)
                    }
                    Divider().background(CyberTheme.border)
                    
                    Text(isDebuggerAttached ?
                         "Cảnh báo: Có tiến trình bên ngoài đang trace app (kinfo_proc.p_flag chứa P_TRACED). Nguy cơ bị debug trộm dữ liệu." :
                         "Rất an toàn: Không phát hiện debugger hoặc lệnh gỡ lỗi nào kết nối vào tiến trình.")
                        .font(.system(size: 12))
                        .foregroundColor(isDebuggerAttached ? CyberTheme.danger : CyberTheme.textSecondary)
                    
                    Text(hasInjectedDyld ?
                         "Cảnh báo: Phát hiện biến môi trường DYLD_INSERT_LIBRARIES." :
                         "Không có thư viện ngoài nào được chèn qua biến môi trường.")
                        .font(.system(size: 12))
                        .foregroundColor(hasInjectedDyld ? CyberTheme.danger : CyberTheme.textSecondary)
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
