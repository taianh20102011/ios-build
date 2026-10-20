import Foundation
import Darwin

public final class AntiDebugDetector {
    
    // Check if process is being traced/debugged via sysctl
    public static func isDebuggerAttached() -> Bool {
        var mib: [Int32] = [CTL_KERN, KERN_PROC, KERN_PROC_PID, getpid()]
        var info = kinfo_proc()
        var size = MemoryLayout<kinfo_proc>.stride
        
        let junk = sysctl(&mib, UInt32(mib.count), &info, &size, nil, 0)
        if junk != 0 {
            return false
        }
        
        return (info.kp_proc.p_flag & P_TRACED) != 0
    }
    
    // Check if DYLD_INSERT_LIBRARIES is present in environment
    public static func hasInjectedDyldEnvironment() -> Bool {
        return getenv("DYLD_INSERT_LIBRARIES") != nil
    }
    
    // Deny debugger attachment dynamically via ptrace (PT_DENY_ATTACH = 31)
    public static func enforceAntiAttach() {
        typealias PtraceType = @convention(c) (CInt, pid_t, CInt, CInt) -> CInt
        let handle = dlopen(nil, RTLD_GLOBAL | RTLD_NOW)
        if let sym = dlsym(handle, "ptrace") {
            let ptraceFunc = unsafeBitCast(sym, to: PtraceType.self)
            _ = ptraceFunc(31, 0, 0, 0) // PT_DENY_ATTACH
        }
        dlclose(handle)
    }
}
