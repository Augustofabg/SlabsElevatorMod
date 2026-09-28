# Elevator Slabs (NeoForge 1.21.1)

**Elevator Slabs** é um addon oficial/compatível para o mod **OpenBlocks Elevator** (`elevatorid` por vsngarcia) desenvolvido especificamente para **Minecraft 1.21.1+** no ecossistema moderno **NeoForge**, pronto para integração com grandes modpacks como **All The Mods 10 (ATM10)** e **All The Mods 11 (ATM11)**.

---

## 🌟 Funcionalidades e Mecânicas

- **16 Variações de Cores**: Inclui todas as 16 cores de corantes do Minecraft, em paridade visual e mecânica com o mod base.
- **Lógica de Teleporte Bidirecional**:
  - Pressionar **Espaço (Jump)** teletransporta para o elevador ou laje de mesma cor acima.
  - Pressionar **Shift (Sneak)** teletransporta para o elevador ou laje de mesma cor abaixo.
  - Interoperabilidade total: teletransporte suave de **Laje ⇄ Bloco Completo** e **Laje ⇄ Laje**.
- **Cálculo de Altura Preciso (Y Offset)**:
  - **Laje Inferior (`BOTTOM`)**: Posiciona os pés do jogador exatamente em `Y + 0.5`.
  - **Laje Superior (`TOP`) / Laje Dupla (`DOUBLE`)**: Posiciona os pés do jogador em `Y + 1.0`.
  - **Bloco Original (`ElevatorBlock`)**: Posiciona os pés do jogador em `Y + 1.0`.
- **Prevenção de Sufocamento e Colisão**:
  - Verificação dinâmica de colisão da bounding box (`level.noCollision`) e verificação de blocos opacos sufocantes na cabeça/olhos do jogador no destino.
- **Tags de Compatibilidade**:
  - `elevatorid:elevators` e `c:elevators` declarados para blocos e itens.
  - `minecraft:slabs` para compatibilidade com outros mods de arquitetura e ferramentas.
- **Item Utilitário Ender Cleaver**:
  - Durabilidade de 15 usos. Permanece na bancada de trabalho (`crafting remainder`) consumindo 1 ponto de dano por operação.
  - Fabricado com 2 linhas e 1 pérola do fim em padrão diagonal (`"  S", " P ", "S  "`).
  - Usado para fatiar 1 bloco de elevador em 2 lajes (`elevatorslabs:elevator_slab_<cor>`).
- **Tela de Configuração Completa (Elevator Options GUI)**:
  - Aberta ao clicar com o botão direito na Elevator Slab com a mão vazia ou agachado (`sneak + use`).
  - Checkbox **Directional**: Força a rotação do jogador para a orientação configurada ao ser teleportado.
  - Seletor em Cruz de **Pontos Cardeais (N, E, S, W)**: Destaque visual em Verde brilhante (`#55FF55`) para a direção selecionada e Branco (`#FFFFFF`) para as inativas.
  - Checkbox **Hide Arrow**: Oculta ou exibe a seta indicativa na laje.
  - Botão **Remove Camouflage**: Limpa o bloco camuflado e devolve-o ao inventário do jogador.
  - Sincronização instantânea entre Client e Server via pacotes de rede NeoForge (`UpdateSlabOptionsPayload`) e persistência NBT na `ElevatorSlabBlockEntity`.
- **Receitas de Crafting**:
  - 1 Elevador original + 1 Ender Cleaver ➔ 2 Elevator Slabs (o cutelo perde 1 ponto de dano).
  - 3 Elevadores originais em linha horizontal ➔ 6 Elevator Slabs.
  - 2 Elevator Slabs em linha vertical ➔ 1 Elevador completo.

---

## 🛠️ Tecnologias e Configuração

- **Plataforma**: NeoForge 1.21.1 (`21.1.130`+)
- **Mod Dev Plugin**: `net.neoforged.moddev` (ModDevGradle `2.0.78`)
- **Java**: JDK 21 (LTS)
- **Dependência CurseMaven**:
  - Project ID: `250832` (OpenBlocks Elevator)
  - NeoForge 1.21.1 File ID: `6199696` (`elevatorid-neoforge-1.21.1-1.11.4.jar`)
  - Coordenada: `curse.maven:openblocks-elevator-250832:6199696`

---

## 📂 Estrutura do Projeto

```
SlabsElevatorMod/
├── LICENSE
├── README.md
├── build.gradle
├── gradle.properties
├── settings.gradle
├── gradlew / gradlew.bat
├── gradle/wrapper/
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties
└── src/main/
    ├── java/net/openslabs/elevatorslabs/
    │   ├── ElevatorSlabsMod.java               # Classe principal @Mod
    │   ├── block/
    │   │   └── ElevatorSlabBlock.java          # Bloco base estendendo SlabBlock
    │   ├── client/
    │   │   └── ElevatorSlabsClientHandler.java # Detecção de Jump/Sneak no cliente
    │   ├── init/
    │   │   ├── ModBlocks.java                  # DeferredRegister dos 16 blocos
    │   │   ├── ModItems.java                   # DeferredRegister dos 16 itens
    │   │   ├── ModCreativeTabs.java            # Aba no modo criativo
    │   │   └── ModTags.java                    # Definições de Tags
    │   ├── network/
    │   │   ├── ElevatorSlabsNetwork.java       # Registro de pacotes NeoForge
    │   │   ├── TeleportSlabPayload.java        # Record CustomPacketPayload
    │   │   └── TeleportSlabHandler.java        # Handler de teleporte seguro no servidor
    │   └── util/
    │       └── ElevatorSearchHelper.java       # Cálculos de Y, busca vertical e colisão
    └── resources/
        ├── META-INF/neoforge.mods.toml         # Manifesto com dependências obrigatórias
        ├── assets/elevatorslabs/
        │   ├── blockstates/                    # 16 arquivos de blockstates
        │   ├── lang/                           # en_us.json e pt_br.json
        │   └── models/                         # 32 modelos de bloco e 16 de item
        └── data/
            ├── c/tags/                         # #c:elevators
            ├── elevatorid/tags/                # #elevatorid:elevators
            ├── elevatorslabs/recipe/           # Receitas de conversão
            └── minecraft/tags/                 # #minecraft:slabs
```

---

## 🚀 Como Compilar e Executar

Execute no terminal (utilizando JDK 21):

```bash
# Compilar o arquivo JAR do mod
./gradlew build

# Executar cliente de teste do Minecraft no ambiente de desenvolvimento
./gradlew runClient
```

O arquivo JAR final será gerado em `build/libs/`.
