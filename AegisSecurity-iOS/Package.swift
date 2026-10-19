// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "AegisSecurity",
    platforms: [
        .iOS(.v16)
    ],
    products: [
        .executable(
            name: "AegisSecurity",
            targets: ["AegisSecurity"]
        )
    ],
    dependencies: [],
    targets: [
        .target(
            name: "AegisSecurity",
            path: "AegisSecurity",
            exclude: ["Resources/Info.plist"]
        )
    ]
)
