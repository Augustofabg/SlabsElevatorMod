<div align="center">

  # Elevator Slabs

  <a href="https://modrinth.com/mod/SEU-MOD">
    <img height="56" alt="Modrinth" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_vector.svg">
  </a>
  <a href="https://minecraftforge.net">
    <img alt="curseforge" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/curseforge_vector.svg">
  </a>

<br/> 

<img alt="forge" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy-minimal/supported/forge_vector.svg">
<img alt="fabric" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy-minimal/supported/fabric_vector.svg">
<img alt="NeoForge" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy-minimal/supported/neoforge_vector.svg">




</div>

  
Addon independente para o mod **[OpenBlocks Elevator](https://github.com/VsnGamer/ElevatorMod)**, que adiciona variantes em laje dos elevadores, permitindo construções mais compactas e integradas ao cenário.

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

## 🚀 Como Compilar e Executar

Este projeto é um addon independente e não possui afiliação oficial com o OpenBlocks Elevator. Todos os créditos pelo mod original pertencem a **vsngarcia** e aos seus colaboradores.
