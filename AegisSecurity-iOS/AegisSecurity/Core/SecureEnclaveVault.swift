import Foundation
import Security
import LocalAuthentication

public struct VaultRecord: Identifiable {
    public let id: String
    public let title: String
    public let content: String
    public let date: Date
    
    public init(id: String, title: String, content: String, date: Date) {
        self.id = id
        self.title = title
        self.content = content
        self.date = date
    }
}

public final class SecureEnclaveVault: ObservableObject {
    private let serviceName = "com.aegis.security.ios.vault"
    
    public init() {}
    
    public func authenticateBiometrics(reason: String, completion: @escaping (Bool, String?) -> Void) {
        let context = LAContext()
        var error: NSError?
        
        if context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error) ||
           context.canEvaluatePolicy(.deviceOwnerAuthentication, error: &error) {
            
            context.evaluatePolicy(.deviceOwnerAuthentication, localizedReason: reason) { success, evalError in
                DispatchQueue.main.async {
                    if success {
                        completion(true, nil)
                    } else {
                        completion(false, evalError?.localizedDescription ?? "Xác thực thất bại")
                    }
                }
            }
        } else {
            completion(false, error?.localizedDescription ?? "Thiết bị chưa cài đặt mã bảo vệ hoặc Face ID")
        }
    }
    
    public func saveItem(title: String, secret: String) -> Bool {
        let id = UUID().uuidString
        guard let data = secret.data(using: .utf8) else { return false }
        
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: serviceName,
            kSecAttrAccount as String: id,
            kSecAttrLabel as String: title,
            kSecValueData as String: data,
            kSecAttrAccessible as String: kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
        ]
        
        SecItemDelete(query as CFDictionary)
        let status = SecItemAdd(query as CFDictionary, nil)
        return status == errSecSuccess
    }
    
    public func fetchAllItems() -> [VaultRecord] {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: serviceName,
            kSecMatchLimit as String: kSecMatchLimitAll,
            kSecReturnAttributes as String: true,
            kSecReturnData as String: true
        ]
        
        var result: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &result)
        
        guard status == errSecSuccess, let items = result as? [[String: Any]] else {
            return []
        }
        
        var list: [VaultRecord] = []
        for item in items {
            let id = item[kSecAttrAccount as String] as? String ?? UUID().uuidString
            let title = item[kSecAttrLabel as String] as? String ?? "Bí mật"
            let data = item[kSecValueData as String] as? Data ?? Data()
            let content = String(data: data, encoding: .utf8) ?? "[Không thể đọc]"
            list.append(VaultRecord(id: id, title: title, content: content, date: Date()))
        }
        return list
    }
    
    public func deleteItem(id: String) {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: serviceName,
            kSecAttrAccount as String: id
        ]
        SecItemDelete(query as CFDictionary)
    }
}
