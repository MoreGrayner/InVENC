## InvENC
해당 프로젝트는 향후 공개될 FLOW 프레임워크의 일부입니다.
완본체에서 기능을 50%정도 제거한 버전입니다.(프로토타입 열화판)

---
> 사용법
 ```kotlin
 InVENC("인벤토리", 슬롯(9배수)){
            setItem(타겟 슬롯, 이름, 리스트(설명), 커스텀 모델데이터, 움직임 여부){
               //실행될 작업
            }
            open(player)//인벤토리 오픈
        }
    }
}
 ```
