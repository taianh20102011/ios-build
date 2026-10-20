import SwiftUI
import UIKit

// Screen Recording Monitor
public final class ScreenRecordingObserver: ObservableObject {
    @Published public var isCaptured: Bool = UIScreen.main.isCaptured
    
    public init() {
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(screenCaptureChanged),
            name: UIScreen.capturedDidChangeNotification,
            object: nil
        )
    }
    
    @objc private func screenCaptureChanged() {
        DispatchQueue.main.async {
            self.isCaptured = UIScreen.main.isCaptured
        }
    }
}

// SwiftUI Secure View Container (Hides content from screenshots and screen recordings)
public struct ScreenShieldView<Content: View>: UIViewRepresentable {
    let content: Content
    
    public init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }
    
    public func makeUIView(context: Context) -> UIView {
        let textField = UITextField()
        textField.isSecureTextEntry = true
        textField.isUserInteractionEnabled = false
        
        guard let secureContainer = textField.subviews.first(where: {
            NSStringFromClass(type(of: $0)).contains("TextLayoutCanvasView") ||
            NSStringFromClass(type(of: $0)).contains("CanvasView")
        }) else {
            let fallbackHosting = UIHostingController(rootView: content)
            fallbackHosting.view.backgroundColor = .clear
            return fallbackHosting.view
        }
        
        let hosting = UIHostingController(rootView: content)
        hosting.view.backgroundColor = .clear
        hosting.view.translatesAutoresizingMaskIntoConstraints = false
        
        secureContainer.addSubview(hosting.view)
        NSLayoutConstraint.activate([
            hosting.view.topAnchor.constraint(equalTo: secureContainer.topAnchor),
            hosting.view.bottomAnchor.constraint(equalTo: secureContainer.bottomAnchor),
            hosting.view.leadingAnchor.constraint(equalTo: secureContainer.leadingAnchor),
            hosting.view.trailingAnchor.constraint(equalTo: secureContainer.trailingAnchor)
        ])
        
        return textField
    }
    
    public func updateUIView(_ uiView: UIView, context: Context) {}
}
