import SwiftUI

public struct SecureVaultView: View {
    @ObservedObject var vault: SecureEnclaveVault
    @State private var isUnlocked: Bool = false
    @State private var items: [VaultRecord] = []
    @State private var showingAddAlert: Bool = false
    @State private var newTitle: String = ""
    @State private var newContent: String = ""
    @State private var authError: String? = nil
    
    public var body: some View {
        VStack(spacing: 16) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text("KÉT SẮT SECURE ENCLAVE")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(CyberTheme.primary)
                    Text("Lưu trữ mật mã phần cứng bảo vệ bởi Face ID")
                        .font(.system(size: 12))
                        .foregroundColor(CyberTheme.textSecondary)
                }
                Spacer()
                if isUnlocked {
                    Button(action: { isUnlocked = false }) {
                        Image(systemName: "lock.fill")
                            .foregroundColor(CyberTheme.warning)
                            .font(.system(size: 18))
                    }
                }
            }
            .padding(.horizontal)
            .padding(.top)
            
            if !isUnlocked {
                Spacer()
                VStack(spacing: 16) {
                    ZStack {
                        RoundedRectangle(cornerRadius: 20)
                            .fill(CyberTheme.card)
                            .frame(width: 80, height: 80)
                        Image(systemName: "faceid")
                            .font(.system(size: 42))
                            .foregroundColor(CyberTheme.primary)
                    }
                    
                    Text("Két Sắt Đang Bị Khóa")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(CyberTheme.textPrimary)
                    
                    Text("Dữ liệu được mã hóa trong chip Secure Enclave của iPhone. Chỉ Face ID, Touch ID hoặc Mật mã máy mới có thể mở.")
                        .font(.system(size: 12))
                        .foregroundColor(CyberTheme.textSecondary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 32)
                    
                    if let err = authError {
                        Text(err)
                            .font(.system(size: 11))
                            .foregroundColor(CyberTheme.danger)
                    }
                    
                    Button(action: unlockVault) {
                        HStack {
                            Image(systemName: "lock.open.fill")
                            Text("MỞ KHÓA BẰNG FACE ID / TOUCH ID")
                                .font(.system(size: 13, weight: .bold))
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 48)
                        .background(CyberTheme.primary)
                        .foregroundColor(.black)
                        .cornerRadius(12)
                        .padding(.horizontal, 32)
                    }
                }
                Spacer()
            } else {
                HStack {
                    Text("DANH SÁCH BÍ MẬT (\(items.count))")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(CyberTheme.textSecondary)
                    Spacer()
                    Button(action: { showingAddAlert = true }) {
                        HStack(spacing: 4) {
                            Image(systemName: "plus")
                            Text("Thêm Mới")
                        }
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(CyberTheme.primary)
                    }
                }
                .padding(.horizontal)
                
                if items.isEmpty {
                    Spacer()
                    Text("Chưa có ghi chú bí mật nào trong Secure Enclave.")
                        .font(.system(size: 13))
                        .foregroundColor(CyberTheme.textSecondary)
                    Spacer()
                } else {
                    List {
                        ForEach(items) { item in
                            HStack {
                                VStack(alignment: .leading, spacing: 4) {
                                    Text(item.title)
                                        .font(.system(size: 14, weight: .bold))
                                        .foregroundColor(CyberTheme.primary)
                                    Text(item.content)
                                        .font(.system(size: 13))
                                        .foregroundColor(CyberTheme.textPrimary)
                                }
                                Spacer()
                            }
                            .listRowBackground(CyberTheme.card)
                        }
                        .onDelete(perform: deleteItem)
                    }
                    .scrollContentBackground(.hidden)
                }
            }
        }
        .background(CyberTheme.background.ignoresSafeArea())
        .sheet(isPresented: $showingAddAlert) {
            AddSecretSheet(onSave: { title, secret in
                _ = vault.saveItem(title: title, secret: secret)
                refresh()
            })
        }
    }
    
    private func unlockVault() {
        vault.authenticateBiometrics(reason: "Mở khóa Két sắt phần cứng Aegis") { success, error in
            if success {
                isUnlocked = true
                authError = nil
                refresh()
            } else {
                authError = error
            }
        }
    }
    
    private func refresh() {
        items = vault.fetchAllItems()
    }
    
    private func deleteItem(at offsets: IndexSet) {
        for index in offsets {
            let item = items[index]
            vault.deleteItem(id: item.id)
        }
        refresh()
    }
}

struct AddSecretSheet: View {
    @Environment(\.dismiss) var dismiss
    @State private var title: String = ""
    @State private var content: String = ""
    let onSave: (String, String) -> Void
    
    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Thông tin bí mật").foregroundColor(CyberTheme.primary)) {
                    TextField("Tiêu đề (VD: Thẻ ngân hàng, Mật khẩu)", text: $title)
                    TextEditor(text: $content)
                        .frame(minHeight: 100)
                }
            }
            .navigationTitle("Lưu Bí Mật Mới")
            .navigationBarItems(
                leading: Button("Hủy") { dismiss() },
                trailing: Button("Lưu") {
                    if !title.isEmpty && !content.isEmpty {
                        onSave(title, content)
                        dismiss()
                    }
                }.disabled(title.isEmpty || content.isEmpty)
            )
        }
    }
}
