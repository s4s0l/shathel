package org.s4s0l.shathel.commons.bin

/**
 * @author Marcin Wielgus
 */
class DefaultBinaryLocators {

    static List<BinaryLocator> getDefaultLocators() {
        [
                new PreinstalledBinaryLocator(
                        "ansible-playbook",
                        "--version",
                        /ansible-playbook (([0-9]+\.?)+)/,
                        "2.2.2.0"
                ),
                new PreinstalledBinaryLocator(
                        "vagrant",
                        "version",
                        /Installed Version: (([0-9]+\.?)+)/,
                        "1.9.3"
                ),
                new DownloadableBinaryLocator(
                        "packer",
                        "1.0.0",
                        "version",
                        /v(([0-9]+\.?)+)/,
                        "https://releases.hashicorp.com/packer/1.0.0/packer_1.0.0_linux_amd64.zip"
                ),
                new DownloadableBinaryLocator(
                        "terraform",
                        "0.9.3",
                        "version",
                        /v(([0-9]+\.?)+)/,
                        "https://releases.hashicorp.com/terraform/0.9.3/terraform_0.9.3_linux_amd64.zip"
                )
        ]
    }


}
