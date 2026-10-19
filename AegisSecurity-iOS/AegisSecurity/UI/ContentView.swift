import SwiftUI

public struct ContentView: View {
    @StateObject private var networkInspector = IOSNetworkInspector()
    @StateObject private var vault = SecureEnclaveVault()
    @StateObject private var screenRecordingObserver = ScreenRecordingObserver()
    
    @State private var jailbreakStatus = JailbreakDetector.checkJailbreak()
    @State private var isDebuggerAttached = AntiDebugDetector.isDebuggerAttached()
    @State private var hasInjectedDyld = AntiDebugDetector.hasInjectedDyldEnvironment()
    @State private var isScanning = false
    @State private var selectedTab = 0
    
    public init() {}
    
    public var body: some View {
        ZStack {
            TabView(selection: $selectedTab) {
                DashboardView(
                    jailbreakStatus: $jailbreakStatus,
                    isDebuggerAttached: $isDebuggerAttached,
                    hasInjectedDyld: $hasInjectedDyld,
                    isScanning: $isScanning,
                    onScan: triggerDeepScan
                )
                .tabItem {
                    Label("Tổng quan", systemImage: "shield.fill")
                }
                .tag(0)
                
                IntegrityView(
                    jailbreakStatus: jailbreakStatus,
                    isDebuggerAttached: isDebuggerAttached,
                    hasInjectedDyld: hasInjectedDyld
                )
                .tabItem {
                    Label("Nguyên bản", systemImage: "lock.shield.fill")
                }
                .tag(1)
                
                NetworkHardeningView(networkInspector: networkInspector)
                .tabItem {
                    Label("Mạng & Gia cố", systemImage: "network")
                }
                .tag(2)
                
                SecureVaultView(vault: vault)
                .tabItem {
                    Label("Két sắt", systemImage: "key.fill")
                }
                .tag(3)
            }
            .accentColor(CyberTheme.primary)
            
            // Screen Recording Shield Overlay
            if screenRecordingObserver.isCaptured {
                ZStack {
                    Color.black.ignoresSafeArea()
                    VStack(spacing: 12) {
                        Image(systemName: "video.slash.fill")
                            .font(.system(size: 48))
                            .foregroundColor(CyberTheme.danger)
                        Text("PHÁT HIỆN GHI MÀN HÌNH")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(CyberTheme.textPrimary)
                        Text("Nội dung bảo mật của Aegis đã được che giấu để bảo vệ quyền riêng tư của bạn.")
                            .font(.system(size: 12))
                            .foregroundColor(CyberTheme.textSecondary)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 32)
                    }
                }
            }
        }
        .onAppear {
            AntiDebugDetector.enforceAntiAttach()
            triggerDeepScan()
        }
    }
    
    private func triggerDeepScan() {
        isScanning = true
        DispatchQueue.global(qos: .userInitiated).async {
            let jb = JailbreakDetector.checkJailbreak()
            let dbg = AntiDebugDetector.isDebuggerAttached()
            let dyld = AntiDebugDetector.hasInjectedDyldEnvironment()
            
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.6) {
                self.jailbreakStatus = jb
                self.isDebuggerAttached = dbg
                self.hasInjectedDyld = dyld
                self.isScanning = false
            }
        }
    }
}
